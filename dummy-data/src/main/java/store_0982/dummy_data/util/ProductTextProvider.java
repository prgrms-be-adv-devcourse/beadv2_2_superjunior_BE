package store_0982.dummy_data.util;

import java.util.concurrent.ThreadLocalRandom;

import store._0982.common.domain.product.ProductCategory;

public final class ProductTextProvider {

    private ProductTextProvider() {
    }

    public static String name(ProductCategory category) {
        return switch (category) {
            case HOME -> combineName(HOME_ADJ, HOME_NOUN);
            case FOOD -> combineName(FOOD_ADJ, FOOD_NOUN);
            case HEALTH -> combineName(HEALTH_ADJ, HEALTH_NOUN);
            case BEAUTY -> combineName(BEAUTY_ADJ, BEAUTY_NOUN);
            case FASHION -> combineName(FASHION_ADJ, FASHION_NOUN);
            case ELECTRONICS -> combineName(ELECTRONICS_ADJ, ELECTRONICS_NOUN);
            case KIDS -> combineName(KIDS_ADJ, KIDS_NOUN);
            case HOBBY -> combineName(HOBBY_ADJ, HOBBY_NOUN);
            case PET -> combineName(PET_ADJ, PET_NOUN);
        };
    }

    public static String description(ProductCategory category) {
        return switch (category) {
            case HOME -> combineDescription(HOME_DESC_PREFIX, HOME_DESC_SUFFIX);
            case FOOD -> combineDescription(FOOD_DESC_PREFIX, FOOD_DESC_SUFFIX);
            case HEALTH -> combineDescription(HEALTH_DESC_PREFIX, HEALTH_DESC_SUFFIX);
            case BEAUTY -> combineDescription(BEAUTY_DESC_PREFIX, BEAUTY_DESC_SUFFIX);
            case FASHION -> combineDescription(FASHION_DESC_PREFIX, FASHION_DESC_SUFFIX);
            case ELECTRONICS -> combineDescription(ELECTRONICS_DESC_PREFIX, ELECTRONICS_DESC_SUFFIX);
            case KIDS -> combineDescription(KIDS_DESC_PREFIX, KIDS_DESC_SUFFIX);
            case HOBBY -> combineDescription(HOBBY_DESC_PREFIX, HOBBY_DESC_SUFFIX);
            case PET -> combineDescription(PET_DESC_PREFIX, PET_DESC_SUFFIX);
        };
    }


    private static String combineName(String[] adjectives, String[] nouns) {
        return pick(adjectives) + " " + pick(nouns);
    }

    private static String combineDescription(String[] prefixes, String[] suffixes) {
        return pick(prefixes) + " " + pick(suffixes);
    }

    private static String pick(String[] options) {
        return options[ThreadLocalRandom.current().nextInt(options.length)];
    }

    private static final String[] HOME_ADJ = {
            "프리미엄", "다용도", "내구성 좋은", "미니멀", "모던", "항균", "무광", "슬림", "대용량", "실속형"
    };
    private static final String[] HOME_NOUN = {
            "수납박스", "도마 세트", "머그컵 세트", "주방행주", "청소 브러시", "수납 바구니",
            "생활용품 세트", "식기 건조대", "욕실 슬리퍼", "다용도 선반"
    };

    private static final String[] FOOD_ADJ = {
            "유기농", "저당", "프리미엄", "수제", "무첨가", "고단백", "고소한", "담백한", "신선한", "프레시"
    };
    private static final String[] FOOD_NOUN = {
            "사과주스", "견과 세트", "그래놀라", "잡곡밥", "블루베리", "그릭요거트",
            "닭가슴살", "수제 쿠키", "한우 등심 500g", "방울토마토"
    };

    private static final String[] HEALTH_ADJ = {
            "고함량", "저자극", "데일리", "프리미엄", "밸런스", "활력", "면역", "수면", "스트레스 케어", "리커버리"
    };
    private static final String[] HEALTH_NOUN = {
            "비타민C 1000", "오메가3 캡슐", "프로바이오틱스", "루테인 복합", "콜라겐",
            "멀티비타민", "체중계", "마사지건", "손목 보호대", "홈트 밴드"
    };

    private static final String[] BEAUTY_ADJ = {
            "수분", "진정", "톤업", "미백", "보습", "저자극", "산뜻한", "쫀쫀한", "비건", "데일리"
    };
    private static final String[] BEAUTY_NOUN = {
            "크림", "선크림", "클렌징폼", "헤어 에센스", "립 틴트", "아이크림",
            "앰플", "마스크팩", "미스트", "핸드크림"
    };

    private static final String[] FASHION_ADJ = {
            "베이직", "클래식", "오버핏", "슬림핏", "캐주얼", "포멀", "니트", "데님", "울 블렌드", "에코 레더"
    };
    private static final String[] FASHION_NOUN = {
            "자켓", "라운드 티", "코트", "슬랙스", "가디건",
            "머플러", "스니커즈", "셔츠", "후드", "에코 레더 백"
    };

    private static final String[] ELECTRONICS_ADJ = {
            "무선", "블루투스", "고성능", "초경량", "게이밍", "멀티포트", "고속 충전",
            "저전력", "스마트", "휴대용"
    };
    private static final String[] ELECTRONICS_NOUN = {
            "이어폰", "스피커", "USB-C 허브", "마우스", "키보드",
            "보조배터리", "스마트 워치", "모니터 암", "웹캠", "노트북 스탠드"
    };

    private static final String[] KIDS_ADJ = {
            "안전한", "부드러운", "유아용", "키즈", "알록달록", "내구성 좋은",
            "저자극", "교육용", "가벼운", "실용적인"
    };
    private static final String[] KIDS_NOUN = {
            "원목 블록", "그림책", "식기 세트", "수면등", "물티슈",
            "내의", "레인부츠", "유아 의자", "스티커북", "장난감 정리함"
    };

    private static final String[] HOBBY_ADJ = {
            "입문용", "전문가용", "빈티지", "아날로그", "디지털", "디테일한",
            "휴대용", "DIY", "프리미엄", "스탠다드"
    };
    private static final String[] HOBBY_NOUN = {
            "미니어처 키트", "수채화 물감", "캘리그래피 펜", "드로잉 패드", "보드게임",
            "퍼즐", "가죽 공예 키트", "모형 키트", "캠핑 랜턴", "드론"
    };

    private static final String[] PET_ADJ = {
            "저알러지", "영양 듬뿍", "프리미엄", "무향", "부드러운", "튼튼한",
            "안전한", "편안한", "대형", "미니"
    };
    private static final String[] PET_NOUN = {
            "고양이 급수기", "강아지 간식", "반려동물 하우스", "고양이 스크래처",
            "강아지 목줄", "반려동물 샴푸", "고양이 장난감", "강아지 방석",
            "반려동물 영양제", "고양이 사료"
    };

    private static final String[] HOME_DESC_PREFIX = {
            "집안에서 유용한 생활 아이템입니다.", "공간 활용을 높여주는 제품입니다.",
            "관리와 보관이 편리하도록 설계했습니다.", "자주 쓰는 생활품을 모았습니다.",
            "정리와 위생을 동시에 챙길 수 있습니다.", "심플한 디자인으로 어디든 잘 어울립니다.",
            "실용성을 강조한 구성입니다.", "장시간 사용해도 부담이 적습니다.",
            "공간을 깔끔하게 유지하는 데 도움됩니다.", "사용 빈도가 높은 제품입니다."
    };
    private static final String[] HOME_DESC_SUFFIX = {
            "깔끔한 정리와 위생 관리에 도움이 됩니다.", "실사용 중심의 구성으로 준비했습니다.",
            "일상에서 자주 쓰는 품목입니다.", "집안 분위기를 해치지 않는 디자인입니다.",
            "내구성을 고려해 제작했습니다.", "관리 부담을 줄여줍니다.",
            "공간 활용도를 높여줍니다.", "오랫동안 사용하기 좋은 제품입니다.",
            "간편하게 사용 가능합니다.", "실속 있게 구성했습니다."
    };

    private static final String[] FOOD_DESC_PREFIX = {
            "신선한 재료로 정성껏 만든 식품입니다.", "간편하게 즐길 수 있는 먹거리입니다.",
            "맛과 품질을 함께 담았습니다.", "바쁜 일상에 맞춘 식품입니다.",
            "부담 없이 즐길 수 있습니다.", "원재료 본연의 맛을 살렸습니다.",
            "균형 잡힌 맛을 제공합니다.", "기분 좋은 식사를 위한 제품입니다.",
            "가족 모두가 즐기기 좋습니다.", "간식과 식사 모두에 적합합니다."
    };
    private static final String[] FOOD_DESC_SUFFIX = {
            "가볍게 한 끼로도 좋습니다.", "냉장/냉동 보관이 용이합니다.",
            "가족과 함께 즐기기 좋습니다.", "바쁜 하루에 간편합니다.",
            "풍미를 오래 유지합니다.", "건강을 생각한 구성입니다.",
            "간식으로도 추천드립니다.", "식사 대용으로도 충분합니다.",
            "다양한 조리에 활용 가능합니다.", "기호에 맞게 즐길 수 있습니다."
    };

    private static final String[] HEALTH_DESC_PREFIX = {
            "일상 속 건강 관리를 돕는 제품입니다.", "활력 있는 하루를 위한 아이템입니다.",
            "꾸준한 섭취에 적합합니다.", "균형 있는 건강 관리를 돕습니다.",
            "부담 없는 건강 루틴을 위한 구성입니다.", "몸 상태를 고려한 제품입니다.",
            "생활 속 건강 습관에 도움 됩니다.", "간편하게 건강을 챙길 수 있습니다.",
            "활동적인 생활을 응원합니다.", "체계적인 관리를 도와줍니다."
    };
    private static final String[] HEALTH_DESC_SUFFIX = {
            "필요한 영양을 챙길 수 있습니다.", "운동과 함께 사용하기 좋습니다.",
            "부담 없이 시작할 수 있습니다.", "데일리 케어에 적합합니다.",
            "꾸준한 사용을 권장합니다.", "다양한 연령대에 어울립니다.",
            "균형 잡힌 구성을 제공합니다.", "생활 루틴에 쉽게 적용됩니다.",
            "필요한 기능에 집중했습니다.", "실용적인 구성입니다."
    };

    private static final String[] BEAUTY_DESC_PREFIX = {
            "피부 결을 매끄럽게 정돈해주는 제품입니다.", "데일리 케어에 적합한 포뮬러입니다.",
            "가볍게 사용할 수 있습니다.", "피부 컨디션을 고려했습니다.",
            "부드럽고 산뜻한 사용감을 제공합니다.", "자극을 줄인 구성을 담았습니다.",
            "피부에 순한 제품입니다.", "매일 사용하기 좋습니다.",
            "보습과 진정을 함께 챙겼습니다.", "간편한 스킨케어를 돕습니다."
    };
    private static final String[] BEAUTY_DESC_SUFFIX = {
            "산뜻한 마무리감을 제공합니다.", "피부 타입에 부담이 적습니다.",
            "보습과 진정에 도움을 줍니다.", "가볍고 끈적임이 덜합니다.",
            "데일리 사용에 적합합니다.", "지속력을 고려했습니다.",
            "맑고 깨끗한 피부를 연출합니다.", "손쉬운 케어에 도움이 됩니다.",
            "촉촉함을 오래 유지합니다.", "피부 컨디션을 안정적으로 유지합니다."
    };

    private static final String[] FASHION_DESC_PREFIX = {
            "데일리 룩에 자연스럽게 어울립니다.", "편안한 착용감을 강조했습니다.",
            "다양한 스타일링이 가능합니다.", "기본에 충실한 디자인입니다.",
            "군더더기 없는 핏을 제공합니다.", "가볍고 편안합니다.",
            "계절감을 고려한 아이템입니다.", "세련된 무드를 더합니다.",
            "활동성을 고려했습니다.", "일상에서 손이 자주 가는 제품입니다."
    };
    private static final String[] FASHION_DESC_SUFFIX = {
            "계절감 있게 활용할 수 있습니다.", "기본 아이템으로 추천드립니다.",
            "활동성을 고려했습니다.", "스타일을 자연스럽게 완성합니다.",
            "가벼운 외출에 잘 어울립니다.", "레이어드에 좋습니다.",
            "편안한 핏을 제공합니다.", "다양한 하의와 매치가 쉽습니다.",
            "적당한 두께감으로 활용도가 높습니다.", "오랫동안 입기 좋습니다."
    };

    private static final String[] ELECTRONICS_DESC_PREFIX = {
            "실용적인 기능과 성능을 갖춘 제품입니다.", "사용 편의성을 높여 설계했습니다.",
            "일상과 업무에 유용합니다.", "기본기에 충실한 제품입니다.",
            "안정적인 사용을 목표로 했습니다.", "휴대성을 고려했습니다.",
            "연결성과 호환성을 강화했습니다.", "실속 있는 구성을 담았습니다.",
            "간편한 사용법을 제공합니다.", "합리적인 선택지입니다."
    };
    private static final String[] ELECTRONICS_DESC_SUFFIX = {
            "안정적인 사용감을 제공합니다.", "휴대성이 좋습니다.",
            "연결성과 호환성을 고려했습니다.", "일상에서 활용도가 높습니다.",
            "작업 효율을 높여줍니다.", "필요한 기능을 갖췄습니다.",
            "편리한 사용을 돕습니다.", "간편하게 사용할 수 있습니다.",
            "오랫동안 사용할 수 있습니다.", "실용적인 선택입니다."
    };

    private static final String[] KIDS_DESC_PREFIX = {
            "아이들의 안전을 고려한 제품입니다.", "부드러운 소재로 제작했습니다.",
            "성장 단계에 맞춰 쓸 수 있습니다.", "아이의 흥미를 끌 수 있습니다.",
            "일상에서 활용하기 좋습니다.", "안전 기준을 고려했습니다.",
            "보호자에게도 편리합니다.", "자주 쓰기 좋은 제품입니다.",
            "아이의 활동성을 돕습니다.", "편안한 사용감을 제공합니다."
    };
    private static final String[] KIDS_DESC_SUFFIX = {
            "사용이 간편하고 관리가 쉽습니다.", "아이들의 흥미를 높입니다.",
            "일상에서 활용도가 높습니다.", "안심하고 사용할 수 있습니다.",
            "부드럽게 사용할 수 있습니다.", "가볍고 편리합니다.",
            "활동량이 많은 아이에게 적합합니다.", "튼튼하게 제작했습니다.",
            "다양한 상황에 어울립니다.", "보호자가 만족할 구성입니다."
    };

    private static final String[] HOBBY_DESC_PREFIX = {
            "취미 시간을 더 즐겁게 만들어 줍니다.", "초보자도 쉽게 시작할 수 있습니다.",
            "완성도를 높여줍니다.", "집중력을 높이는 데 도움이 됩니다.",
            "디테일을 살렸습니다.", "즐거운 경험을 제공합니다.",
            "창의적인 활동에 어울립니다.", "혼자서도 충분히 즐길 수 있습니다.",
            "기본부터 응용까지 가능합니다.", "취미 입문에 적합합니다."
    };
    private static final String[] HOBBY_DESC_SUFFIX = {
            "선물용으로도 좋습니다.", "디테일한 결과물을 만들 수 있습니다.",
            "간편하게 준비할 수 있습니다.", "집중력을 높이는 데 도움이 됩니다.",
            "시간이 지나도 가치가 있습니다.", "취미의 폭을 넓혀줍니다.",
            "만족도가 높은 편입니다.", "완성 후 성취감을 줍니다.",
            "사용법이 간단합니다.", "꾸준히 즐기기 좋습니다."
    };

    private static final String[] PET_DESC_PREFIX = {
            "반려동물의 건강과 행복을 고려했습니다.", "편안한 사용감을 제공하는 제품입니다.",
            "반려동물 케어에 유용합니다.", "일상 케어에 잘 어울립니다.",
            "위생과 관리에 도움 됩니다.", "안심하고 사용할 수 있습니다.",
            "반려동물의 스트레스를 줄여줍니다.", "안정적인 사용을 고려했습니다.",
            "실용적인 구성을 담았습니다.", "편리함을 강조했습니다."
    };
    private static final String[] PET_DESC_SUFFIX = {
            "관리와 사용이 간편합니다.", "안전하게 사용할 수 있습니다.",
            "일상 케어에 잘 어울립니다.", "반려동물의 만족도를 높입니다.",
            "꾸준히 사용하기 좋습니다.", "편안함을 제공합니다.",
            "깨끗하게 관리할 수 있습니다.", "오랫동안 사용 가능합니다.",
            "활동성을 고려했습니다.", "케어 루틴에 적합합니다."
    };
}
