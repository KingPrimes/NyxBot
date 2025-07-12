package com.nyx.bot.enums;

import lombok.Getter;

@Getter
public enum StateTypeEnum {
    ALL("","未知"),
    GEAR("","道具"),
    KEYS("","钥匙"),
    RESOURCES("","资源"),
    SENTINELS("","守护/宠物"),
    OTHER("","加成"),
    MODS("", "MOD"),
    WARFRAMES("", "战甲"),
    WEAPONS("", "武器"),
    RELIC_BRONZE("/Lotus/Types/Game/Projections/.*?(Bronze)$", "完整遗物"),
    RELIC_PLATINUM("/Lotus/Types/Game/Projections/.*?(Platinum)$", "光辉遗物"),
    RELIC_GOLD("/Lotus/Types/Game/Projections/.*?(Gold)$", "无暇遗物"),
    RELIC_SILVER("/Lotus/Types/Game/Projections/.*?(Silver)$", "优良遗物"),
    ENHANCERS("/Lotus/Upgrades/CosmeticEnhancers/.*","赋能"),
    SKINS("/Lotus/Upgrades/Skins/.*", "外观"),
    SHIP("/Lotus/Types/Ship/.*", "采集机"),
    TENNO_ACCESSORY_SCARVES("/Lotus/Characters/Tenno/Accessory/Scarves/.*", "披饰"),
    WEAPONS_TENNO_MELEE_SKIN("/Lotus/Weapons/Tenno/Melee/.*Skin", "武器外观"),
    KUBROW_PET_PATTERNS("/Lotus/Types/Game/KubrowPet/Patterns/.*", "库狛花纹"),
    CATBROW_PET_PATTERNS("/Lotus/Types/Game/CatbrowPet/Patterns/.*", "库娃花纹"),
    INFESTED_KAVAT_PET_PATTERNS("/Lotus/Types/Game/InfestedKavatPet/Patterns/.*", "狐帕菲拉花纹"),
    INFESTED_PREDATORS_PET_PATTERNS("/Lotus/Types/Game/InfestedPredatorPet/Patterns/.*", "扑猎达赛花纹"),
    BACKGROUNDS("/Lotus/Interface/Graphics/CustomUI/Backgrounds/.*", "背景"),
    CURSORS("/Lotus/Interface/Graphics/CustomUI/Cursors/.*", "指针"),
    SOUNDS("/Lotus/Interface/Graphics/CustomUI/Sounds/.*", "登录音效"),
    CUSTOM_UI_STYLE("/Lotus/Interface/Graphics/CustomUI/.*Style", "主题"),
    ACTION_FIGURE_DIORAMAS("/Lotus/Types/Game/ActionFigureDioramas/.*", "景观"),
    COLORS("/Lotus/Types/Game/(.*)/?Colors/.*", "颜色"),
    NOTE_PACkS("/Lotus/Types/Game/NotePacks/.*", "乐器"),
    POSE_SETS("/Lotus/Types/Game/PoseSets/.*", "姿势组"),
    QUARTERS_WALLPAPERS("/Lotus/Types/Game/QuartersWallpapers/.*", "壁纸模板"),
    ARCADE("/Lotus/Types/Items/Arcade/.*", "街机"),
    EMOTES("/Lotus/Types/Items/Emotes/.*", "表情"),
    VIDEO_WALL_BACKDROPS("/Lotus/Types/Items/VideoWallBackdrops/.*", "视频墙背景"),
    VIDEO_WALL_SOUNDSCAPES("/Lotus/Types/Items/VideoWallSoundscapes/.*", "视频墙音景"),
    AVATAR_IMAGES("/Lotus/Types/StoreItems/AvatarImages/.*", "浮印"),
    SUIT_CUSTOMIZATIONS("/Lotus/Types/StoreItems/SuitCustomizations/.*", "颜色包");

    final String NAME;
    final String KEY;

    StateTypeEnum(String key, String name) {
        this.KEY = key;
        this.NAME = name;
    }
}
