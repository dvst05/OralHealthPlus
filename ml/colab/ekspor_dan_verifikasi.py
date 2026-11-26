"""
Ekspor ulang TFLite dan uji kesetaraannya dengan model Keras pada SELURUH data validasi.

Mengapa: ekspor lama (Optimize.DEFAULT = kuantisasi dynamic range) hanya sama kelas pada 46/50
foto, dengan selisih probabilitas maksimum 0,2128. Aplikasi memakai TFLite, jadi akurasi dan
tabel ambang di laporan harus dihitung dari keluaran TFLite.

Cara pakai (Colab, setelah semua sel notebook dijalankan sehingga `model`, `val_ds`,
dan `class_names` tersedia):
    from ekspor_dan_verifikasi import jalankan
    jalankan(model, val_ds, class_names)
Salin file hasil yang dipilih menjadi app/src/main/assets/oralhealth_mobilenetv2.tflite.
Input dan output tetap float32, jadi Classifier.kt tidak perlu diubah.
"""
import numpy as np
import tensorflow as tf

AMBANG = (0.50, 0.60, 0.70, 0.80, 0.85, 0.90)


def konversi(model, mode):
    c = tf.lite.TFLiteConverter.from_keras_model(model)
    if mode == "float16":
        c.optimizations = [tf.lite.Optimize.DEFAULT]
        c.target_spec.supported_types = [tf.float16]
    elif mode != "float32":
        raise ValueError(mode)
    return c.convert()


def prediksi_tflite(isi_model, x):
    it = tf.lite.Interpreter(model_content=isi_model)
    it.allocate_tensors()
    masuk = it.get_input_details()[0]["index"]
    keluar = it.get_output_details()[0]["index"]
    hasil = []
    for foto in x:
        it.set_tensor(masuk, foto[None].astype(np.float32))
        it.invoke()
        hasil.append(it.get_tensor(keluar)[0].copy())
    return np.stack(hasil)


def jalankan(model, val_ds, class_names, mode_list=("float16", "float32")):
    # Kumpulkan foto dan label dalam SATU iterasi supaya urutannya konsisten
    xs, ys = [], []
    for xb, yb in val_ds:
        xs.append(xb.numpy())
        ys.append(np.argmax(yb.numpy(), axis=1))
    x, y = np.concatenate(xs), np.concatenate(ys)
    p_keras = model.predict(x, verbose=0)
    print(f"Keras    : akurasi {np.mean(p_keras.argmax(1) == y):.4f} ({len(y)} foto)")

    ringkasan = {}
    for mode in mode_list:
        isi = konversi(model, mode)
        nama = f"oralhealth_mobilenetv2_{mode}.tflite"
        with open(nama, "wb") as f:
            f.write(isi)
        p = prediksi_tflite(isi, x)
        sama = int(np.sum(p.argmax(1) == p_keras.argmax(1)))
        selisih = float(np.max(np.abs(p - p_keras)))
        akurasi = float(np.mean(p.argmax(1) == y))
        ringkasan[mode] = dict(file=nama, sama=sama, total=len(y), selisih_maks=selisih, akurasi=akurasi)
        print(f"\n{mode:8s} : {len(isi) / 1e6:.2f} MB | akurasi {akurasi:.4f} | "
              f"kelas sama {sama}/{len(y)} | selisih maks {selisih:.4f}")
        print("ambang | diterima | akurasi pada yang diterima")
        conf = p.max(1)
        for t in AMBANG:
            ok = conf >= t
            acc = np.mean(p.argmax(1)[ok] == y[ok]) if ok.any() else float("nan")
            print(f"{t:5.2f}  | {ok.mean():8.1%} | {acc:8.1%}")

    with open("labels.txt", "w") as f:
        f.write("\n".join(class_names))
    return ringkasan
