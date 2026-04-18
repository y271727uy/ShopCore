package com.list.util;

import com.list.ListMod;
import org.slf4j.Logger;

public class LogUtil {
    private static final String PREFIX = "[ListMod]";
    private static final Logger LOGGER = ListMod.LOGGER;

    private static final String INFO_ONE = "温馨提示：为了稳定性与兼容性考虑,建议您避免使用下列mod";
    private static final String INFO_TWO = "避免使用有可疑的行为的mod";
    private static final String INFO_THREE = "避免使用完全由AI制作mod，因无法保证作者是否有能力进行长期维护";
    private static final String INFO_END = "如果您使用了上述mod,请务必在使用过程中注意备份存档,以免发生不必要的损失且本整合包不会为任何因此导致的错误而负责";

    public static void info(String message) {
        LOGGER.info(PREFIX + " " + INFO_ONE);
        LOGGER.info(PREFIX + " " + INFO_TWO);
        LOGGER.info(PREFIX + " " + INFO_THREE);
        LOGGER.info(PREFIX + " " + INFO_END);
    }
}
//希望能有帮助去避免问题
/*
某些情况下指名道姓是一个需要避免的问题，但是这俩emmmm....有点太离谱了
这俩主要让我决定写的理由其实很简单，下面分类说明x
1. z
尽管已经移除了AI提示词污染但是行为很令人诧异（虽然影响范围有限）但是能够做出这样的事情，后面我很难再次信任他
毕竟谁也不知道哪天又会冒出来一个AI提示词误导进群（（
*此外这个效率很低的

2. s
并不是我否定ai，写这些话的时候copilot还在一直补全(下面注释内容甚至是补全的)。我的观点也一直是不要完全依赖ai
毕竟它也不是万能的，完全由ai制作的mod更是一个未知数。我认为应当标注由ai制作（事实上ai项目是很显眼的当然这不重要）
此外在项目负责上我认为这类ai项目是很难保证长期维护的，毕竟它们的维护者可能并不具备足够的能力来处理复杂的问题

此外，我觉得其实z可以有更好的解决方案，与其污染不如再顺手告诉用户这个污染提示词在哪里，可以让用户手动删除

至于s，在MmdSkin的12楼下面我看到了虽然ai很强了但是我觉得s有点太过于信任ai了
我觉得稍微花点时间学习下、找人问问再使用ai应该能有更多的提升，毕竟知名度摆在这。阡陌交通其实还不错的，但是我希望能够优化好，让更多性能可能一般的玩家也体验到这个mod

最后就是尽管可能有争议性但是我认为在特定定位下有一定的必要性（毕竟是整合包定制，主要还是为了整合包稳定性考虑）
随便写了点理由，我觉得有必要提一嘴也不算是小作文吧（？）真理解成小作文我也没办法了，我觉得雨晴那种能把正常讨论能变成网络暴力的人还是比较少的

最后的最后，我已经尽可能的削弱可能的攻击性了，互联网并非法外之地。
如果有因此而产生的争议性我的处理是要根据事实情况的，如果认为冒犯可以联系我删除。但如果类似于雨晴（已向公安机关备案）演变为任何形式的网络暴力（包括但不限于人身攻击、诽谤、造谣、开盒等）我将保留追究法律责任的权利。
*/

/*
public static void warn(String message) {
    LOGGER.warn(PREFIX + " " + message);
}

public static void error(String message) {
    LOGGER.error(PREFIX + " " + message);
}

public static void debug(String message) {
    LOGGER.debug(PREFIX + " " + message);
}

这一堆玩意暂时是用不上
*/