package com.oneinstep.ddd.asset.value;

/**
 * 三元组-用于幂等
 *
 * @param fromSourceType  来源类型-用于幂等
 * @param fromSourceId    来源ID-用于幂等
 * @param fromSourceSubId 来源子ID-用于幂等
 */
public record FromSource(Integer fromSourceType, Long fromSourceId, Long fromSourceSubId) {

    public static FromSource of(Integer fromSourceType, Long fromSourceId, Long fromSourceSubId) {
        return new FromSource(fromSourceType, fromSourceId, fromSourceSubId);
    }
}
