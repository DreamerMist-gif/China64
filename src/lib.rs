use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::{jdouble, jlong};
use rand::{Rng, SeedableRng};
use rand_chacha::ChaCha8Rng;
use std::collections::hash_map::DefaultHasher;
use std::hash::{Hash, Hasher};

// 这里复用之前的逻辑，简化为函数
#[derive(Debug, Clone, Copy, PartialEq)]
enum YaoType {
    OldYin = 6, YoungYang = 7, YoungYin = 8, OldYang = 9,
}

impl YaoType {
    fn generate(rng: &mut ChaCha8Rng) -> Self {
        let sum: u8 = (0..3).map(|_| rng.gen_range(0..=1)).sum();
        match sum {
            0 => YaoType::OldYin,
            1 => YaoType::YoungYang,
            2 => YaoType::YoungYin,
            3 => YaoType::OldYang,
            _ => unreachable!(),
        }
    }
    
    fn to_original_bit(&self) -> u8 {
        match self { YaoType::OldYin => 0, YaoType::YoungYang => 1, YaoType::YoungYin => 0, YaoType::OldYang => 1 }
    }

    fn to_changed_bit(&self) -> u8 {
        match self { YaoType::OldYin => 1, YaoType::YoungYang => 1, YaoType::YoungYin => 0, YaoType::OldYang => 0 }
    }
}

fn get_gua_name(binary: u8) -> &'static str {
    match binary {
        63 => "乾为天", 0 => "坤为地", 34 => "水雷屯", 17 => "山水蒙",
        58 => "水天需", 23 => "天水讼", 16 => "地水师", 2 => "水地比",
        59 => "风天小畜", 55 => "天泽履", 56 => "地天泰", 7 => "天地否",
        47 => "天火同人", 61 => "火天大有", 8 => "地山谦", 4 => "雷地豫",
        38 => "泽雷随", 25 => "山风蛊", 48 => "地泽临", 3 => "风地观",
        37 => "火雷噬嗑", 41 => "山火贲", 1 => "山地剥", 32 => "地雷复",
        39 => "天雷无妄", 57 => "山天大畜", 33 => "山雷颐", 30 => "泽风大过",
        18 => "坎为水", 45 => "离为火", 14 => "泽山咸", 28 => "雷风恒",
        15 => "天山遁", 60 => "雷天大壮", 5 => "火地晋", 40 => "地火明夷",
        43 => "风火家人", 53 => "火泽睽", 10 => "水山蹇", 20 => "雷水解",
        49 => "山泽损", 35 => "风雷益", 62 => "泽天夬", 31 => "天风姤",
        6 => "泽地萃", 24 => "地风升", 22 => "泽水困", 26 => "水风井",
        29 => "泽火革", 46 => "火风鼎", 36 => "震为雷", 9 => "艮为山",
        11 => "风山渐", 52 => "雷泽归妹", 44 => "雷火丰", 13 => "火山旅",
        27 => "巽为风", 54 => "兑为泽", 19 => "风水涣", 50 => "水泽节",
        51 => "风泽中孚", 12 => "雷山小过", 42 => "水火既济", 21 => "火水未济",
        _ => "未知",
    }
}

// JNI 导出函数
// 对应 Java 类名: com.example.rustiching.MainActivity
// 方法名: getDivination
#[no_mangle]
pub extern "system" fn Java_com_example_rustiching_MainActivity_getDivination(
    env: JNIEnv,
    _class: JClass,
    azimuth: jdouble,
    timestamp: jlong,
    acceleration: jdouble,
) -> JString {
    // 1. 生成种子
    let mut hasher = DefaultHasher::new();
    azimuth.to_bits().hash(&mut hasher);
    timestamp.hash(&mut hasher);
    acceleration.to_bits().hash(&mut hasher);
    let seed = hasher.finish();

    // 2. 起卦
    let mut rng = ChaCha8Rng::seed_from_u64(seed);
    let mut lines = Vec::new();
    for _ in 0..6 {
        lines.push(YaoType::generate(&mut rng));
    }

    // 3. 构造输出字符串
    let mut result = String::new();
    let mut hex_nums = Vec::new();
    
    // 从上往下打印（视觉习惯），但存储是从初爻开始
    result.push_str(&format!("种子: {}\n\n", seed));
    
    for (i, yao) in lines.iter().enumerate().rev() {
        let line_str = match yao {
            YaoType::OldYin => "——  —— X",
            YaoType::YoungYang => "——————  ",
            YaoType::YoungYin => "——  ——  ",
            YaoType::OldYang => "—————— O",
        };
        result.push_str(&format!("{}爻({}): {}\n", if i==5 {"上"} else if i==0 {"初"} else {"  "}, *yao as u8, line_str));
        hex_nums.push(*yao as u8);
    }
    
    // 计算本卦变卦
    let mut orig_bits = 0u8;
    let mut chg_bits = 0u8;
    for (i, yao) in lines.iter().enumerate() {
        if yao.to_original_bit() == 1 { orig_bits |= 1 << i; }
        if yao.to_changed_bit() == 1 { chg_bits |= 1 << i; }
    }
    
    result.push_str(&format!("\n序列: {:?}\n", hex_nums)); // 注意：hex_nums这里是逆序显示的，如果需要正序请自行调整
    result.push_str(&format!("本卦: {}\n", get_gua_name(orig_bits)));
    result.push_str(&format!("变卦: {}", get_gua_name(chg_bits)));

    // 返回 Java 字符串
    env.new_string(result).expect("Couldn't create java string!").into()
}
