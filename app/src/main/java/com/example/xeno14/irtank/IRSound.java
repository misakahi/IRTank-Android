package com.example.xeno14.irtank;

import java.util.Arrays;

/**
 * NECフォーマットの赤外線信号に対応するPCMデータを生成するクラス
 */
public class IRSound {
  public static final int SAMPLE_RATE = 44100;  // 44.1kHz
  static final int IR_FREQ = 19000;             // 19kHz

  // Parameters for WAVE
  public static final int WAVE_BITS = 16;
  public static final int WAVE_BYTES = WAVE_BITS / 8;
  public static final int STEREO = 2;

  // NEC format
  public static final int BITS       =    32;  // micro sec
  public static final int HDR_MARK   =  9000;  // micro sec
  public static final int HDR_SPACE  =  4500;  // micro sec
  public static final int BIT_MARK   =   560;  // micro sec
  public static final int ONE_SPACE  =  1690;  // micro sec
  public static final int ZERO_SPACE =   560;  // micro sec

  byte[] hdr_mark_arr;
  byte[] hdr_space_arr;
  byte[] bit_mark_arr;
  byte[] one_space_buf;
  byte[] zero_space_buf;
  byte[] buffer;

  public IRSound() {
    hdr_mark_arr   = new byte[(int)(HDR_MARK   * STEREO * WAVE_BYTES * SAMPLE_RATE / 1e6)];
    hdr_space_arr  = new byte[(int)(HDR_SPACE  * STEREO * WAVE_BYTES * SAMPLE_RATE / 1e6)];  // always 0
    bit_mark_arr   = new byte[(int)(BIT_MARK   * STEREO * WAVE_BYTES * SAMPLE_RATE / 1e6)];
    one_space_buf  = new byte[(int)(ONE_SPACE  * STEREO * WAVE_BYTES * SAMPLE_RATE / 1e6)];  // always 0
    zero_space_buf = new byte[(int)(ZERO_SPACE * STEREO * WAVE_BYTES * SAMPLE_RATE / 1e6)];  // always 0

    // 使用する最大サイズを確保しておく 
    buffer = new byte[hdr_mark_arr.length + hdr_space_arr.length +
      (bit_mark_arr.length + Math.max(one_space_buf.length, zero_space_buf.length)) * BITS];

    // LEDを点灯させる部分に波形を設定
    fillWave(hdr_mark_arr, hdr_mark_arr.length);
    fillWave(bit_mark_arr, bit_mark_arr.length);
  }

  /**
   * バッファに正弦波のデータを設定する
   */
  public void fillWave(byte[] buf, int length) {
    final int sample_length = length / STEREO / WAVE_BYTES;
    System.out.println("sample length:" + sample_length);
    for (int i=0; i < sample_length; i++) {
      double t = (double)i / SAMPLE_RATE;
      short v = (short)(Short.MAX_VALUE * Math.sin(2*Math.PI* IR_FREQ *t)); // 正弦波

      // STEREO=2, WAVE_BYTES=2を仮定
      int index = i * STEREO * WAVE_BYTES;

      // little endian
      // left
      buf[index]     = (byte)(v & 0xff);          // 下位8bit
      buf[index + 1] = (byte)((v >>> 8 ) & 0xff); // 上位8bit
      // // right
      buf[index + 2] = (byte)(-buf[index]);
      buf[index + 3] = (byte)(-buf[index + 1]);
    }
  }

  /**
   * srcの内容でdestを埋める
   */
  public int fillArray(byte[] src, byte[] dest, int dest_offset) {
    System.arraycopy(src, 0, dest, dest_offset, src.length);
    return dest_offset + src.length;
  }

  /**
   * 32bit整数値に対応する波形をバッファに設定する
   *
   * @param val 32bit整数値
   * @return 信号のデータの長さ
     */
  public int setValue(int val) {
    int offset = 0;

    // リーダー部の書き込み
    offset = fillArray(hdr_mark_arr, buffer, offset);
    offset = fillArray(hdr_space_arr, buffer, offset);

    // 赤外線の信号はbig endian
    for (int mask = 1 << (BITS - 1);  mask != 0;  mask = mask >>> 1) {
      // high
      offset = fillArray(bit_mark_arr, buffer, offset);

      // bitによりlowの長さを変える
      if ((val & mask) != 0) {
        //System.out.println("HIGH");
        offset = fillArray(one_space_buf, buffer, offset);
      } else {
        //System.out.println("LOW");
        offset = fillArray(zero_space_buf, buffer, offset);
      }
    }
    // terminate
    offset = fillArray(bit_mark_arr, buffer, offset);

    // 残りを0で埋める
    Arrays.fill(buffer, offset, buffer.length, (byte) 0);

    return offset;
  }

  /**
   * setValueのエイリアス
   * @param val
   * @return バッファ長
     */
  public int setValue32bit(int val) {
    return setValue(val);
  }

  /**
   * 16bit整数値とエラー検出用のビット反転値を設定する
   *
   * @param val 16bit整数値
   * @return バッファ長
     */
  public int setValue16bit(short val) {
    short not_val = (short)~val;
    int val32 = (val << 16) + not_val;
    return setValue32bit(val32);
  }

  /**
   * 信号のバッファのゲッター
   */
  public byte[] getByteArray() {
    return buffer;
  }
}
