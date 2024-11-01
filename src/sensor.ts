import { SensorStatus } from './definitions';
import {
  PRESENCE_DETECTED,
  LIGHT_CONTROL_REGULATOR_ACCURACY,
  OUTPUT_RIPPLE_VOLTAGE_SPECIFICATION,
  INPUT_VOLTAGE_RIPPLE_SPECIFICATION,
  OUTPUT_CURRENT_PERCENT,
  LUMEN_MAINTENANCE_FACTOR,
  MOTION_SENSED,
  MOTION_THRESHOLD,
  PRESENT_DEVICE_OPERATING_EFFICIENCY,
  PRESENT_RELATIVE_OUTPUT_RIPPLE_VOLTAGE,
  PRESENT_INPUT_RIPPLE_VOLTAGE,
  DESIRED_AMBIENT_TEMPERATURE,
  PRESENT_AMBIENT_TEMPERATURE,
  PRESENT_INDOOR_AMBIENT_TEMPERATURE,
  PRESENT_OUTDOOR_AMBIENT_TEMPERATURE,
  PRECISE_PRESENT_AMBIENT_TEMPERATURE,
  PRESENT_DEVICE_OPERATING_TEMPERATURE,
  PEOPLE_COUNT,
  PRESENT_AMBIENT_RELATIVE_HUMIDITY,
  PRESENT_INDOOR_RELATIVE_HUMIDITY,
  PRESENT_OUTDOOR_RELATIVE_HUMIDITY,
  LIGHT_CONTROL_LIGHTNESS_ON,
  LIGHT_CONTROL_LIGHTNESS_PROLONG,
  LIGHT_CONTROL_LIGHTNESS_STANDBY,
  TIME_SINCE_MOTION_SENSED,
  TIME_SINCE_PRESENCE_DETECTED,
  LIGHT_SOURCE_START_COUNTER_RESETTABLE,
  LIGHT_SOURCE_TOTAL_POWER_ON_CYCLES,
  RATED_MEDIAN_USEFUL_LIGHT_SOURCE_STARTS,
  TOTAL_DEVICE_OFF_ON_CYCLES,
  TOTAL_DEVICE_POWER_ON_CYCLES,
  TOTAL_DEVICE_STARTS,
  LIGHT_CONTROL_AMBIENT_LUX_LEVEL_ON,
  LIGHT_CONTROL_AMBIENT_LUX_LEVEL_PROLONG,
  LIGHT_CONTROL_AMBIENT_LUX_LEVEL_STANDBY,
  PRESENT_AMBIENT_LIGHT_LEVEL,
  PRESENT_ILLUMINANCE,
  DEVICE_RUNTIME_SINCE_TURN_ON,
  DEVICE_RUNTIME_WARRANTY,
  RATED_MEDIAN_USEFUL_LIFE_OF_LUMINAIRE,
  TOTAL_DEVICE_POWER_ON_TIME,
  TOTAL_DEVICE_RUNTIME,
  TOTAL_LIGHT_EXPOSURE_TIME,
  LIGHT_CONTROL_TIME_FADE,
  LIGHT_CONTROL_TIME_FADE_ON,
  LIGHT_CONTROL_TIME_FADE_STANDBY_AUTO,
  LIGHT_CONTROL_TIME_FADE_STANDBY_MANUAL,
  LIGHT_CONTROL_TIME_OCCUPANCY_DELAY,
  LIGHT_CONTROL_TIME_PROLONG,
  LIGHT_CONTROL_TIME_RUN_ON,
  DEVICE_DATE_OF_MANUFACTURE,
  LUMINAIRE_TIME_OF_MANUFACTURE,
  PRESSURE,
  AIR_PRESSURE,
  LIGHT_CONTROL_REGULATOR_KID,
  LIGHT_CONTROL_REGULATOR_KIU,
  LIGHT_CONTROL_REGULATOR_KPU,
  LIGHT_CONTROL_REGULATOR_KPD,
  SENSOR_GAIN,
  DEVICE_HARDWARE_REVISION,
  DEVICE_SERIAL_NUMBER,
  DEVICE_MODEL_NUMBER,
  LUMINAIRE_COLOR,
  LUMINAIRE_IDENTIFICATION_NUMBER,
  DEVICE_MANUFACTURER_NAME,
  LUMINAIRE_IDENTIFICATION_STRING,
  ACTIVE_ENERGY_LOAD_SIDE,
  PRECISE_TOTAL_DEVICE_ENERGY_USE,
  ACTIVE_POWER_LOAD_SIDE,
  LUMINAIRE_NOMINAL_INPUT_POWER,
  LUMINAIRE_POWER_AT_MINIMUM_DIM_LEVEL,
  PRESENT_DEVICE_INPUT_POWER,
  PRESENT_INPUT_CURRENT,
  PRESENT_OUTPUT_CURRENT,
} from './properties';

export const SENSOR_FORMAT_A = 0x00;
export const SENSOR_FORMAT_B = 0x01;

export type SensorDataType = boolean | number | Date | string | Uint8Array;

export abstract class SensorData<T extends SensorDataType> {
  private _propertyId: number;
  protected _value!: T | null;

  public get propertyId () {
    return this._propertyId;
  }

  public get value () {
    return this._value;
  }

  protected constructor (propertyId: number) {
    this._propertyId = propertyId;
    this._value = null;
  }

  protected isLittleEndian () {
    const ua = new ArrayBuffer(4);
    const u8a = new Uint8Array(ua);
    const u32a = new Uint32Array(ua);

    u32a[0] = 0x01020304;
    return u8a[0] == 0x01;
  }

  public abstract setValue(value: T | Uint8Array): void;
  public abstract toBytes(): Uint8Array;

  public toValues (name?: string): any {
    const values: Array<number> = [];
    this.toBytes().forEach(v => values.push(v));

    return {
      [name || 'propertyId']: this.propertyId,
      values: values,
    };
  }

  // static method
  private static rcls: Map<number, any> = new Map();

  public static register (propertyId: number, cls: any) {
    SensorData.rcls.set(propertyId, cls);
  }

  public static from (
    src: SensorStatus | number,
    ...args: any
  ): SensorData<SensorDataType> | Array<SensorData<SensorDataType>> {
    if (typeof src == 'number') {
      const CLS = CLSM[src];
      if (CLS) return new CLS(src, ...args);

      const RCLS = SensorData.rcls.get(src);
      if (RCLS) return new RCLS(src, ...args);

      return new Unknown(src);
    }

    let offset, format, length, propertyId;
    const u8a = Uint8Array.from(src.data);
    const res: Array<SensorData<SensorDataType>> = [];

    offset = 0;
    while (offset < u8a.byteLength) {
      const octet0 = u8a[offset++] & 0xff;
      const octet1 = u8a[offset++] & 0xff;

      format = octet0 & 0x01;
      if (format == SENSOR_FORMAT_A) {
        length = ((octet0 & 0x1e) >> 1) + 1;
        propertyId = (octet1 << 3) | (octet0 >> 5);
      } else {
        const octet2 = u8a[offset++] & 0xff;
        const tlen = (octet0 & 0xfe) >> 1;
        length = tlen == 0x7f ? 0 : tlen;
        propertyId = (octet2 << 8) | octet1;
      }

      const ins = SensorData.from(
        propertyId,
        ...args,
      ) as SensorData<SensorDataType>;
      res.push(ins);

      ins.setValue(u8a.slice(offset, offset + length));
      offset += length;
    }

    return res.length == 1 ? res[0] : res;
  }
}

export class Bool extends SensorData<boolean> {
  public constructor (propertyId: number) {
    super(propertyId);
  }

  public setValue (value: boolean | Uint8Array): void {
    if (typeof value == 'boolean') {
      this._value = value;
      return;
    }

    if (value.byteLength == 0) this._value = false;
    else this._value = value[0] == 0x01;
  }
  public toBytes (): Uint8Array {
    const u8a = new Uint8Array(1);
    u8a[0] = this._value ? 0x01 : 0x00;
    return u8a;
  }
}

export class Percentage8 extends SensorData<number> {
  public constructor (propertyId: number) {
    super(propertyId);
  }

  public setValue (value: number | Uint8Array): void {
    if (typeof value == 'number') {
      this._value = value;
      return;
    }

    this._value = unsignedToSigned(value[0], 8) / 2.0;
  }

  public toBytes (): Uint8Array {
    const u8a = new Uint8Array(1);
    if (this._value == null) {
      u8a[0] = 0xff;
    } else {
      u8a[0] = parseInt((this._value * 2.0).toString());
    }
    return u8a;
  }
}

export type TemperatureType = 'Celsius' | 'Fahrenheit';

export class Temperature extends SensorData<number> {
  private _type: TemperatureType;

  public get type () {
    return this._type;
  }

  public constructor (propertyId: number, type?: TemperatureType) {
    super(propertyId);
    this._type = type || 'Celsius';
  }

  public setValue (value: number | Uint8Array): void {
    if (typeof value == 'number') {
      this._value = value;
      return;
    }

    const u8a = value;
    let tval;
    if (u8a.byteLength == 1) {
      tval = unsignedToSigned(u8a[0], 8);
      if (tval == 0x8000) this._value = null;
      else this._value = tval / 2.0;
      this._type = 'Celsius';
    } else if (u8a.byteLength == 2) {
      tval = unsignedToSigned((u8a[1] << 8) | u8a[0], 16);
      if (tval == 0x8000) this._value = null;
      else this._value = tval / 100.0;
      this._type = 'Fahrenheit';
    }
  }

  public toBytes (): Uint8Array {
    const u8a = new Uint8Array(this._type == 'Celsius' ? 1 : 2);
    if (this._type == 'Celsius') {
      const val = parseInt(((this._value || 0) * 2).toString());
      u8a[0] = val & 0xff;
    } else {
      const val = parseInt(((this._value || 0) * 100).toString());
      u8a[0] = (val >> 0) & 0xff;
      u8a[1] = (val >> 8) & 0xff;
    }
    return u8a;
  }
}

export class Count extends SensorData<number> {
  public constructor (propertyId: number) {
    super(propertyId);
  }

  public setValue (value: number | Uint8Array): void {
    if (typeof value == 'number') {
      this._value = value;
      return;
    }

    this._value = 0;
    if (value.byteLength == 2) {
      this._value |= value[0] << 0;
      this._value |= value[1] << 8;
    } else if (value.byteLength == 3) {
      this._value |= value[0] << 16;
      this._value |= value[1] << 8;
      this._value |= value[2] << 0;
    }
  }
  public toBytes (): Uint8Array {
    const m16 = 0xffff;
    const val = this._value as number;
    const u8a = new Uint8Array(val > m16 ? 3 : 2);

    if (val > m16) {
      u8a[0] = (val >> 16) & 0xff;
      u8a[1] = (val >> 8) & 0xff;
      u8a[2] = (val >> 0) & 0xff;
    } else {
      u8a[0] = (val >> 0) & 0xff;
      u8a[1] = (val >> 8) & 0xff;
    }

    return u8a;
  }
}

export class Humidity extends SensorData<number> {
  public constructor (propertyId: number) {
    super(propertyId);
  }

  public setValue (value: number | Uint8Array): void {
    if (typeof value == 'number') {
      this._value = value;
      return;
    }

    this._value = 0;
    this._value |= value[0] << 0;
    this._value |= value[1] << 8;
    this._value /= 100.0;
  }
  public toBytes (): Uint8Array {
    const val = parseInt(((this._value as number) * 100).toString());
    const u8a = new Uint8Array(2);

    u8a[0] = (val >> 0) & 0xff;
    u8a[1] = (val >> 8) & 0xff;

    return u8a;
  }
}

export class PerceivedLightness extends SensorData<number> {
  public constructor (propertyId: number) {
    super(propertyId);
  }

  public setValue (value: number | Uint8Array): void {
    if (typeof value == 'number') {
      this._value = value;
      return;
    }

    this._value = 0;
    this._value |= value[0] << 0;
    this._value |= value[1] << 8;
  }
  public toBytes (): Uint8Array {
    const val = parseInt((this._value as number).toString());
    const u8a = new Uint8Array(2);

    u8a[0] = (val >> 0) & 0xff;
    u8a[1] = (val >> 8) & 0xff;

    return u8a;
  }
}

export class TimeSecond extends SensorData<number> {
  public constructor (propertyId: number) {
    super(propertyId);
  }

  public setValue (value: number | Uint8Array): void {
    if (typeof value == 'number') {
      this._value = value;
      return;
    }

    const len = value.byteLength;
    this._value = 0;
    if (len == 1) {
      this._value |= value[0];
    } else if (len == 2) {
      this._value |= value[0] << 0;
      this._value |= value[1] << 8;
    } else if (len == 4) {
      this._value |= value[0] << 0;
      this._value |= value[1] << 8;
      this._value |= value[2] << 16;
      this._value |= value[3] << 24;
    }
  }
  public toBytes (): Uint8Array {
    const val = this._value as number;
    let len = 1;
    if (val > 0xffff) len = 2;
    if (val > 0xffffffff) len = 4;

    const u8a = new Uint8Array(len);
    if (len == 1) {
      u8a[0] = (val >> 0) & 0xff;
    } else if (len == 2) {
      u8a[0] = (val >> 0) & 0xff;
      u8a[1] = (val >> 8) & 0xff;
    } else if (len == 4) {
      u8a[0] = (val >> 0) & 0xff;
      u8a[1] = (val >> 8) & 0xff;
      u8a[2] = (val >> 16) & 0xff;
      u8a[3] = (val >> 24) & 0xff;
    }
    return u8a;
  }
}

export class Illuminance extends SensorData<number> {
  public constructor (propertyId: number) {
    super(propertyId);
  }

  public setValue (value: number | Uint8Array): void {
    if (typeof value == 'number') {
      this._value = value;
      return;
    }

    this._value = 0;
    this._value |= value[0] << 0;
    this._value |= value[1] << 8;
    this._value |= value[2] << 16;
    this._value /= 100.0;
  }
  public toBytes (): Uint8Array {
    const val = parseInt(((this._value as number) * 100).toString());
    const u8a = new Uint8Array(3);

    u8a[0] = (val >> 0) & 0xff;
    u8a[1] = (val >> 8) & 0xff;
    u8a[2] = (val >> 16) & 0xff;

    return u8a;
  }
}

export class TimeHour24 extends SensorData<number> {
  public constructor (propertyId: number) {
    super(propertyId);
  }

  public setValue (value: number | Uint8Array): void {
    if (typeof value == 'number') {
      this._value = value;
      return;
    }

    this._value = 0;
    this._value |= value[0] << 0;
    this._value |= value[1] << 8;
    this._value |= value[2] << 16;
  }
  public toBytes (): Uint8Array {
    const val = parseInt((this._value as number).toString());
    const u8a = new Uint8Array(3);

    u8a[0] = (val >> 0) & 0xff;
    u8a[1] = (val >> 8) & 0xff;
    u8a[2] = (val >> 16) & 0xff;

    return u8a;
  }
}

export class TimeMillisecond24 extends SensorData<number> {
  public constructor (propertyId: number) {
    super(propertyId);
  }

  public setValue (value: number | Uint8Array): void {
    if (typeof value == 'number') {
      this._value = value;
      return;
    }

    this._value = 0;
    this._value |= value[0] << 0;
    this._value |= value[1] << 8;
    this._value |= value[2] << 16;
    this._value /= 1000.0;
  }
  public toBytes (): Uint8Array {
    const val = parseInt(((this._value as number) * 1000).toString());
    const u8a = new Uint8Array(3);

    u8a[0] = (val >> 0) & 0xff;
    u8a[1] = (val >> 8) & 0xff;
    u8a[2] = (val >> 16) & 0xff;

    return u8a;
  }
}

export class DateUtc extends SensorData<Date> {
  public constructor (propertyId: number) {
    super(propertyId);
  }

  public setValue (value: Date | Uint8Array): void {
    if (value instanceof Date) {
      this._value = value;
      return;
    }

    let val = 0;
    val |= value[0] << 0;
    val |= value[1] << 8;
    val |= value[2] << 16;
    val *= 86400000;
    this._value = new Date(val);
  }
  public toBytes (): Uint8Array {
    const time = (this._value as Date).getTime();
    const val = parseInt((time / 86400000).toString());
    const u8a = new Uint8Array(3);

    u8a[0] = (val >> 0) & 0xff;
    u8a[1] = (val >> 8) & 0xff;
    u8a[2] = (val >> 16) & 0xff;

    return u8a;
  }
}

export class Pressure extends SensorData<number> {
  public constructor (propertyId: number) {
    super(propertyId);
  }

  public setValue (value: number | Uint8Array): void {
    if (typeof value == 'number') {
      this._value = value;
      return;
    }

    this._value = 0;
    this._value |= value[0] << 0;
    this._value |= value[1] << 8;
    this._value |= value[2] << 16;
    this._value |= value[3] << 24;
    this._value /= 10.0;
  }
  public toBytes (): Uint8Array {
    const val = parseInt(((this._value as number) * 10).toString());
    const u8a = new Uint8Array(4);

    u8a[0] = (val >> 0) & 0xff;
    u8a[1] = (val >> 8) & 0xff;
    u8a[2] = (val >> 16) & 0xff;
    u8a[3] = (val >> 24) & 0xff;

    return u8a;
  }
}

export class Coefficient extends SensorData<number> {
  public constructor (propertyId: number) {
    super(propertyId);
  }

  public setValue (value: number | Uint8Array): void {
    if (typeof value == 'number') {
      this._value = value;
      return;
    }

    if (this.isLittleEndian()) {
      const f32a = new Float32Array(value);
      this._value = f32a[0];
    } else {
      const f32a = new Float32Array(value.reverse());
      this._value = f32a[0];
    }
  }
  public toBytes (): Uint8Array {
    const val = this._value as number;
    const ua = new ArrayBuffer(4);
    const u8a = new Uint8Array(ua);
    const f32a = new Float32Array(ua);

    f32a[0] = val;
    if (this.isLittleEndian()) {
      return u8a;
    } else {
      return u8a.reverse();
    }
  }
}

export class FixedString extends SensorData<string> {
  public constructor (propertyId: number) {
    super(propertyId);
  }

  public setValue (value: string | Uint8Array): void {
    if (typeof value == 'string') {
      this._value = value;
      return;
    }

    this._value = String.fromCharCode(...value);
  }
  public toBytes (): Uint8Array {
    const vals = (this._value as string).split('');
    const raws = Uint8Array.from(vals.map(c => c.charCodeAt(0)));

    let flen = raws.byteLength;
    let len = 0;
    if (flen > 0 && flen <= 8) {
      len = 8;
    } else if (flen > 8 && flen <= 16) {
      len = 16;
    } else if (flen > 16 && flen <= 24) {
      len = 24;
    } else if (flen > 24 && flen <= 36) {
      len = 36;
    } else if (flen > 36 && flen <= 64) {
      len = 64;
    }

    const u8a = new Uint8Array(len);

    raws.forEach((v, i) => {
      u8a[i] = v;
    });

    return u8a;
  }
}

export class Energy32 extends SensorData<number> {
  public constructor (propertyId: number) {
    super(propertyId);
  }

  public setValue (value: number | Uint8Array): void {
    if (typeof value == 'number') {
      this._value = value;
      return;
    }

    this._value = 0;
    this._value |= value[0] << 0;
    this._value |= value[1] << 8;
    this._value |= value[2] << 16;
    this._value |= value[3] << 24;

    this._value /= 1000.0;
  }
  public toBytes (): Uint8Array {
    const val = parseInt(((this._value as number) * 1000).toString());
    const u8a = new Uint8Array(4);

    u8a[0] = (val >> 0) & 0xff;
    u8a[1] = (val >> 8) & 0xff;
    u8a[2] = (val >> 16) & 0xff;
    u8a[3] = (val >> 24) & 0xff;

    return u8a;
  }
}

export class Power extends SensorData<number> {
  public constructor (propertyId: number) {
    super(propertyId);
  }

  public setValue (value: number | Uint8Array): void {
    if (typeof value == 'number') {
      this._value = value;
      return;
    }

    this._value = 0;
    this._value |= value[0] << 0;
    this._value |= value[1] << 8;
    this._value |= value[2] << 16;
    this._value /= 10.0;
  }
  public toBytes (): Uint8Array {
    const val = parseInt(((this._value as number) * 10).toString());
    const u8a = new Uint8Array(3);

    u8a[0] = (val >> 0) & 0xff;
    u8a[1] = (val >> 8) & 0xff;
    u8a[2] = (val >> 16) & 0xff;

    return u8a;
  }
}

export class ElectricCurrent extends SensorData<number> {
  public constructor (propertyId: number) {
    super(propertyId);
  }

  public setValue (value: number | Uint8Array): void {
    if (typeof value == 'number') {
      this._value = value;
      return;
    }

    this._value = 0;
    this._value |= value[0] << 0;
    this._value |= value[1] << 8;
    this._value /= 100.0;
  }
  public toBytes (): Uint8Array {
    const val = parseInt(((this._value as number) * 100).toString());
    const u8a = new Uint8Array(3);

    u8a[0] = (val >> 0) & 0xff;
    u8a[1] = (val >> 8) & 0xff;
    u8a[2] = (val >> 16) & 0xff;

    return u8a;
  }
}

export class Unknown extends SensorData<Uint8Array> {
  public constructor (propertyId: number) {
    super(propertyId);
    this._value = new Uint8Array(0);
  }

  public setValue (value: Uint8Array): void {
    this._value = value;
  }
  public toBytes (): Uint8Array {
    return this._value as Uint8Array;
  }
}

export class Uint8 extends SensorData<number> {
  public constructor (propertyId: number) {
    super(propertyId);
  }

  public setValue (value: number | Uint8Array): void {
    if (typeof value == 'number') {
      this._value = value;
      return;
    }

    this._value = 0;
    this._value |= value[0] << 0;
  }

  public toBytes (): Uint8Array {
    const val = parseInt((this._value as number).toString());
    const u8a = new Uint8Array(1);

    u8a[0] = (val >> 0) & 0xff;

    return u8a;
  }
}

export class Uint16 extends SensorData<number> {
  public constructor (propertyId: number) {
    super(propertyId);
  }

  public setValue (value: number | Uint8Array): void {
    if (typeof value == 'number') {
      this._value = value;
      return;
    }

    this._value = 0;
    this._value |= value[0] << 0;
    this._value |= value[1] << 8;
  }

  public toBytes (): Uint8Array {
    const val = parseInt((this._value as number).toString());
    const u8a = new Uint8Array(2);

    u8a[0] = (val >> 0) & 0xff;
    u8a[1] = (val >> 8) & 0xff;

    return u8a;
  }
}

export class Uint32 extends SensorData<number> {
  public constructor (propertyId: number) {
    super(propertyId);
  }

  public setValue (value: number | Uint8Array): void {
    if (typeof value == 'number') {
      this._value = value;
      return;
    }

    this._value = 0;
    this._value |= value[0] << 0;
    this._value |= value[1] << 8;
    this._value |= value[2] << 16;
    this._value |= value[3] << 24;
  }

  public toBytes (): Uint8Array {
    const val = parseInt((this._value as number).toString());
    const u8a = new Uint8Array(4);

    u8a[0] = (val >> 0) & 0xff;
    u8a[1] = (val >> 8) & 0xff;
    u8a[2] = (val >> 16) & 0xff;
    u8a[3] = (val >> 24) & 0xff;

    return u8a;
  }
}

export class Int8 extends SensorData<number> {
  public constructor (propertyId: number) {
    super(propertyId);
  }

  public setValue (value: number | Uint8Array): void {
    if (typeof value == 'number') {
      this._value = value;
      return;
    }

    this._value = 0;
    this._value |= value[0] << 0;
    this._value = unsignedToSigned(this._value, 8);
  }

  public toBytes (): Uint8Array {
    const int = parseInt((this._value as number).toString());
    const val = signedToUnsigned(int, 8);
    const u8a = new Uint8Array(1);

    u8a[0] = (val >> 0) & 0xff;

    return u8a;
  }
}

export class Int16 extends SensorData<number> {
  public constructor (propertyId: number) {
    super(propertyId);
  }

  public setValue (value: number | Uint8Array): void {
    if (typeof value == 'number') {
      this._value = value;
      return;
    }

    this._value = 0;
    this._value |= value[0] << 0;
    this._value |= value[1] << 8;
    this._value = unsignedToSigned(this._value, 16);
  }

  public toBytes (): Uint8Array {
    const int = parseInt((this._value as number).toString());
    const val = signedToUnsigned(int, 16);
    const u8a = new Uint8Array(2);

    u8a[0] = (val >> 0) & 0xff;
    u8a[1] = (val >> 8) & 0xff;

    return u8a;
  }
}

export class Int32 extends SensorData<number> {
  public constructor (propertyId: number) {
    super(propertyId);
  }

  public setValue (value: number | Uint8Array): void {
    if (typeof value == 'number') {
      this._value = value;
      return;
    }

    this._value = 0;
    this._value |= value[0] << 0;
    this._value |= value[1] << 8;
    this._value |= value[2] << 16;
    this._value |= value[3] << 24;
    this._value = unsignedToSigned(this._value, 32);
  }

  public toBytes (): Uint8Array {
    const int = parseInt((this._value as number).toString());
    const val = signedToUnsigned(int, 32);
    const u8a = new Uint8Array(4);

    u8a[0] = (val >> 0) & 0xff;
    u8a[1] = (val >> 8) & 0xff;
    u8a[2] = (val >> 16) & 0xff;
    u8a[3] = (val >> 24) & 0xff;

    return u8a;
  }
}

export class Uint16Value extends SensorData<number> {
  private _exponent: number;

  public get exponent () {
    return this._exponent;
  }

  public constructor (propertyId: number, exponent?: number) {
    super(propertyId);
    this._exponent = exponent || 0;
  }

  public setValue (value: number | Uint8Array): void {
    if (typeof value == 'number') {
      this._value = value;
      return;
    }

    this._value = 0;
    this._value |= value[0] << 0;
    this._value |= value[1] << 8;
    this._value /= Math.pow(10, this._exponent);
  }
  public toBytes (): Uint8Array {
    const int = (this._value as number) * Math.pow(10, this._exponent);
    const val = parseInt(int.toString());
    const u8a = new Uint8Array(2);

    u8a[0] = (val >> 0) & 0xff;
    u8a[1] = (val >> 8) & 0xff;

    return u8a;
  }
}

export class Uint32Value extends SensorData<number> {
  private _exponent: number;

  public get exponent () {
    return this._exponent;
  }

  public constructor (propertyId: number, exponent?: number) {
    super(propertyId);
    this._exponent = exponent || 0;
  }

  public setValue (value: number | Uint8Array): void {
    if (typeof value == 'number') {
      this._value = value;
      return;
    }

    this._value = 0;
    this._value |= value[0] << 0;
    this._value |= value[1] << 8;
    this._value |= value[2] << 16;
    this._value |= value[3] << 24;
    this._value /= Math.pow(10, this._exponent);
  }
  public toBytes (): Uint8Array {
    const int = (this._value as number) * Math.pow(10, this._exponent);
    const val = parseInt(int.toString());
    const u8a = new Uint8Array(4);

    u8a[0] = (val >> 0) & 0xff;
    u8a[1] = (val >> 8) & 0xff;
    u8a[2] = (val >> 16) & 0xff;
    u8a[3] = (val >> 24) & 0xff;

    return u8a;
  }
}

export class Int16Value extends SensorData<number> {
  private _exponent: number;

  public get exponent () {
    return this._exponent;
  }

  public constructor (propertyId: number, exponent?: number) {
    super(propertyId);
    this._exponent = exponent || 0;
  }

  public setValue (value: number | Uint8Array): void {
    if (typeof value == 'number') {
      this._value = value;
      return;
    }

    this._value = 0;
    this._value |= value[0] << 0;
    this._value |= value[1] << 8;
    this._value = unsignedToSigned(this._value, 16);
    this._value /= Math.pow(10, this._exponent);
  }
  public toBytes (): Uint8Array {
    const int = (this._value as number) * Math.pow(10, this._exponent);
    const val = signedToUnsigned(parseInt(int.toString()), 16);
    const u8a = new Uint8Array(2);

    u8a[0] = (val >> 0) & 0xff;
    u8a[1] = (val >> 8) & 0xff;

    return u8a;
  }
}

export class Int32Value extends SensorData<number> {
  private _exponent: number;

  public get exponent () {
    return this._exponent;
  }

  public constructor (propertyId: number, exponent?: number) {
    super(propertyId);
    this._exponent = exponent || 0;
  }

  public setValue (value: number | Uint8Array): void {
    if (typeof value == 'number') {
      this._value = value;
      return;
    }

    this._value = 0;
    this._value |= value[0] << 0;
    this._value |= value[1] << 8;
    this._value |= value[2] << 16;
    this._value |= value[3] << 24;
    this._value = unsignedToSigned(this._value, 32);
    this._value /= Math.pow(10, this._exponent);
  }
  public toBytes (): Uint8Array {
    const int = (this._value as number) * Math.pow(10, this._exponent);
    const val = signedToUnsigned(parseInt(int.toString()), 32);
    const u8a = new Uint8Array(4);

    u8a[0] = (val >> 0) & 0xff;
    u8a[1] = (val >> 8) & 0xff;
    u8a[2] = (val >> 16) & 0xff;
    u8a[3] = (val >> 24) & 0xff;

    return u8a;
  }
}

function unsignedToSigned (unsigned: number, size: number) {
  if ((unsigned & (1 << (size - 1))) != 0) {
    unsigned = -1 * ((1 << (size - 1)) - (unsigned & ((1 << (size - 1)) - 1)));
  }
  return unsigned;
}

function signedToUnsigned (signed: number, size: number) {
  if (signed < 0) {
    signed = (1 << size) + signed;
    signed &= (1 << size) - 1;
  }
  return signed;
}

const CLSM: any = {
  [PRESENCE_DETECTED]: Bool,
  [LIGHT_CONTROL_REGULATOR_ACCURACY]: Percentage8,
  [OUTPUT_RIPPLE_VOLTAGE_SPECIFICATION]: Percentage8,
  [INPUT_VOLTAGE_RIPPLE_SPECIFICATION]: Percentage8,
  [OUTPUT_CURRENT_PERCENT]: Percentage8,
  [LUMEN_MAINTENANCE_FACTOR]: Percentage8,
  [MOTION_SENSED]: Percentage8,
  [MOTION_THRESHOLD]: Percentage8,
  [PRESENT_DEVICE_OPERATING_EFFICIENCY]: Percentage8,
  [PRESENT_RELATIVE_OUTPUT_RIPPLE_VOLTAGE]: Percentage8,
  [PRESENT_INPUT_RIPPLE_VOLTAGE]: Percentage8,
  [DESIRED_AMBIENT_TEMPERATURE]: Temperature,
  [PRESENT_AMBIENT_TEMPERATURE]: Temperature,
  [PRESENT_INDOOR_AMBIENT_TEMPERATURE]: Temperature,
  [PRESENT_OUTDOOR_AMBIENT_TEMPERATURE]: Temperature,
  [PRECISE_PRESENT_AMBIENT_TEMPERATURE]: Temperature,
  [PRESENT_DEVICE_OPERATING_TEMPERATURE]: Temperature,
  [PEOPLE_COUNT]: Count,
  [PRESENT_AMBIENT_RELATIVE_HUMIDITY]: Humidity,
  [PRESENT_INDOOR_RELATIVE_HUMIDITY]: Humidity,
  [PRESENT_OUTDOOR_RELATIVE_HUMIDITY]: Humidity,
  [LIGHT_CONTROL_LIGHTNESS_ON]: PerceivedLightness,
  [LIGHT_CONTROL_LIGHTNESS_PROLONG]: PerceivedLightness,
  [LIGHT_CONTROL_LIGHTNESS_STANDBY]: PerceivedLightness,
  [TIME_SINCE_MOTION_SENSED]: TimeSecond,
  [TIME_SINCE_PRESENCE_DETECTED]: TimeSecond,
  [LIGHT_SOURCE_START_COUNTER_RESETTABLE]: Count,
  [LIGHT_SOURCE_TOTAL_POWER_ON_CYCLES]: Count,
  [RATED_MEDIAN_USEFUL_LIGHT_SOURCE_STARTS]: Count,
  [TOTAL_DEVICE_OFF_ON_CYCLES]: Count,
  [TOTAL_DEVICE_POWER_ON_CYCLES]: Count,
  [TOTAL_DEVICE_STARTS]: Count,
  [LIGHT_CONTROL_AMBIENT_LUX_LEVEL_ON]: Illuminance,
  [LIGHT_CONTROL_AMBIENT_LUX_LEVEL_PROLONG]: Illuminance,
  [LIGHT_CONTROL_AMBIENT_LUX_LEVEL_STANDBY]: Illuminance,
  [PRESENT_AMBIENT_LIGHT_LEVEL]: Illuminance,
  [PRESENT_ILLUMINANCE]: Illuminance,
  [DEVICE_RUNTIME_SINCE_TURN_ON]: TimeHour24,
  [DEVICE_RUNTIME_WARRANTY]: TimeHour24,
  [RATED_MEDIAN_USEFUL_LIFE_OF_LUMINAIRE]: TimeHour24,
  [TOTAL_DEVICE_POWER_ON_TIME]: TimeHour24,
  [TOTAL_DEVICE_RUNTIME]: TimeHour24,
  [TOTAL_LIGHT_EXPOSURE_TIME]: TimeHour24,
  [LIGHT_CONTROL_TIME_FADE]: TimeMillisecond24,
  [LIGHT_CONTROL_TIME_FADE_ON]: TimeMillisecond24,
  [LIGHT_CONTROL_TIME_FADE_STANDBY_AUTO]: TimeMillisecond24,
  [LIGHT_CONTROL_TIME_FADE_STANDBY_MANUAL]: TimeMillisecond24,
  [LIGHT_CONTROL_TIME_OCCUPANCY_DELAY]: TimeMillisecond24,
  [LIGHT_CONTROL_TIME_PROLONG]: TimeMillisecond24,
  [LIGHT_CONTROL_TIME_RUN_ON]: TimeMillisecond24,
  [DEVICE_DATE_OF_MANUFACTURE]: DateUtc,
  [LUMINAIRE_TIME_OF_MANUFACTURE]: DateUtc,
  [PRESSURE]: Pressure,
  [AIR_PRESSURE]: Pressure,
  [LIGHT_CONTROL_REGULATOR_KID]: Coefficient,
  [LIGHT_CONTROL_REGULATOR_KIU]: Coefficient,
  [LIGHT_CONTROL_REGULATOR_KPD]: Coefficient,
  [LIGHT_CONTROL_REGULATOR_KPU]: Coefficient,
  [SENSOR_GAIN]: Coefficient,
  [DEVICE_HARDWARE_REVISION]: FixedString,
  [DEVICE_SERIAL_NUMBER]: FixedString,
  [DEVICE_MODEL_NUMBER]: FixedString,
  [LUMINAIRE_COLOR]: FixedString,
  [LUMINAIRE_IDENTIFICATION_NUMBER]: FixedString,
  [DEVICE_MANUFACTURER_NAME]: FixedString,
  [LUMINAIRE_IDENTIFICATION_STRING]: FixedString,
  [ACTIVE_ENERGY_LOAD_SIDE]: Energy32,
  [PRECISE_TOTAL_DEVICE_ENERGY_USE]: Energy32,
  [ACTIVE_POWER_LOAD_SIDE]: Power,
  [LUMINAIRE_NOMINAL_INPUT_POWER]: Power,
  [LUMINAIRE_POWER_AT_MINIMUM_DIM_LEVEL]: Power,
  [PRESENT_DEVICE_INPUT_POWER]: Power,
  [PRESENT_INPUT_CURRENT]: ElectricCurrent,
  [PRESENT_OUTPUT_CURRENT]: ElectricCurrent,
};
