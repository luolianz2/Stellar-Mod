package com.luolian.stellarmod.api.compat;

import com.luolian.stellarmod.StellarMod;
import com.luolian.stellarmod.api.util.OriginsUtil;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;

public interface StellarTaskTypes {
    TaskType ORIGIN = TaskTypes.register(
            StellarMod.location("origin"),
            OriginsTask::new,
            OriginsUtil::getOriginIcon
    );

    static void init() {}
}
