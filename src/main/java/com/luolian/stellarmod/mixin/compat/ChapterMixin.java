package com.luolian.stellarmod.mixin.compat;

import com.luolian.stellarmod.api.util.DependencyChecker;
import com.luolian.stellarmod.api.internal.ChapterDependencyController;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.util.ConfigQuestObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Mixin(value = Chapter.class, remap = false)
public class ChapterMixin implements ChapterDependencyController {
    @Shadow @Final public BaseQuestFile file;

    @Unique
    private final List<QuestObject> stellar$dependencies = new ArrayList<>(0);
    @Unique
    private DependencyRequirement stellar$dependencyRequirement = DependencyRequirement.ONE_STARTED;
    @Unique
    private boolean stellar$invisibleUntilDependenciesCompleted = true;
    @Unique
    private boolean stellar$lockUntilDependenciesCompleted = true;

    @Override
    public boolean stellar$hasDependencies() {
        return !stellar$dependencies.isEmpty();
    }

    @Override
    public void stellar$addDependency(QuestObject dependency) {
        this.stellar$dependencies.add(dependency);
    }

    @Override
    public void stellar$clearDependencies() {
        this.stellar$dependencies.clear();
    }

    @Override
    public boolean stellar$isInvisibleUntilDependenciesCompleted() {
        return this.stellar$invisibleUntilDependenciesCompleted;
    }

    @Override
    public void stellar$setInvisibleUntilDependenciesCompleted(boolean invisible) {
        this.stellar$invisibleUntilDependenciesCompleted = invisible;
    }

    @Override
    public boolean stellar$isLockUntilDependenciesCompleted() {
        return this.stellar$lockUntilDependenciesCompleted;
    }

    @Override
    public void stellar$setLockUntilDependenciesCompleted(boolean lock) {
        this.stellar$lockUntilDependenciesCompleted = lock;
    }

    @Override
    public List<QuestObject> stellar$getDependencies() {
        return this.stellar$dependencies;
    }

    @Unique
    @Override
    public void stellar$setDependencyRequirement(DependencyRequirement requirement) {
        this.stellar$dependencyRequirement = requirement;
    }

    @Unique
    @Override
    public DependencyRequirement stellar$getDependencyRequirement() {
        return this.stellar$dependencyRequirement;
    }

    @Unique
    @Override
    public boolean stellar$isLocked(TeamData teamData) {
        if (!this.stellar$hasDependencies()) return false;
        return !this.stellar$areDependenciesCompleted(teamData);
    }

    @Unique
    private boolean stellar$checkDependency(DependencyChecker checker) {
        if (this.stellar$dependencyRequirement.needOnlyOne()) {
            return this.stellar$dependencies.stream().anyMatch(checker::check);
        } else {
            return this.stellar$dependencies.stream().allMatch(checker::check);
        }
    }

    @Unique
    private boolean stellar$areDependenciesCompleted(TeamData teamData) {
        return this.stellar$checkDependency(dependency -> this.stellar$dependencyRequirement.needCompletion() ? teamData.isCompleted(dependency) : teamData.isStarted(dependency));
    }

    @Inject(
            method = "fillConfigGroup",
            at = @At("TAIL")
    )
    private void onFillConfigGroup(ConfigGroup config, CallbackInfo ci) {
        Chapter self = (Chapter) (Object) this;
        ConfigGroup dependencies = config.getOrCreateSubgroup("dependencies");
        Predicate<QuestObjectBase> dependenciesTypes = questObjectBase -> questObjectBase != this.file && questObjectBase != self && questObjectBase instanceof QuestObject;
        dependencies.addList(
                "dependencies",
                this.stellar$dependencies,
                new ConfigQuestObject<>(dependenciesTypes),
                null
        );
        dependencies.addEnum(
                "dependency_requirement",
                this.stellar$dependencyRequirement,
                v -> this.stellar$dependencyRequirement = v,
                DependencyRequirement.NAME_MAP
        );
        dependencies.addBool(
                "invisible",
                this.stellar$invisibleUntilDependenciesCompleted,
                v -> this.stellar$invisibleUntilDependenciesCompleted = v,
                true
        );
        dependencies.addBool(
                "lock",
                this.stellar$lockUntilDependenciesCompleted,
                v -> this.stellar$lockUntilDependenciesCompleted = v,
                true
        );
    }

    @Inject(
            method = "isVisible",
            at = @At("RETURN"),
            cancellable = true
    )
    private void onIsVisible(TeamData data, CallbackInfoReturnable<Boolean> cir) {
        // 原来就不可见
        if (!cir.getReturnValue()) return;
        // 不需要在依赖完成前隐藏
        if (!this.stellar$invisibleUntilDependenciesCompleted) return;

        if (this.stellar$isLocked(data)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "writeData",
            at = @At("TAIL")
    )
    private void onWriteData(CompoundTag nbt, CallbackInfo ci) {
        if (this.stellar$hasDependencies()) {
            ListTag dependencies = new ListTag();

            for(QuestObject dependency : this.stellar$dependencies) {
                dependencies.add(StringTag.valueOf(dependency.getCodeString()));
            }

            nbt.put("stellar$dependencies", dependencies);
        }
        nbt.putString("stellar$dependencyRequirement", this.stellar$dependencyRequirement.getId());
        nbt.putBoolean("stellar$invisibleUntilDependenciesCompleted", this.stellar$invisibleUntilDependenciesCompleted);
        nbt.putBoolean("stellar$lockUntilDependenciesCompleted", this.stellar$lockUntilDependenciesCompleted);
    }

    @Inject(
            method = "readData",
            at = @At("TAIL")
    )
    private void onReadData(CompoundTag nbt, CallbackInfo ci) {
        ListTag dependencies = nbt.getList("stellar$dependencies", 8);
        this.stellar$clearDependencies();
        for(int i = 0; i < dependencies.size(); ++i) {
            QuestObject questObject = this.file.get(this.file.getID(dependencies.getString(i)));
            if (questObject != null) {
                this.stellar$addDependency(questObject);
            }
        }

        this.stellar$dependencyRequirement = DependencyRequirement.NAME_MAP.get(nbt.getString("stellar$dependencyRequirement"));
        this.stellar$invisibleUntilDependenciesCompleted = nbt.getBoolean("stellar$invisibleUntilDependenciesCompleted");
        this.stellar$lockUntilDependenciesCompleted = nbt.getBoolean("stellar$lockUntilDependenciesCompleted");
    }

    @Inject(
            method = "writeNetData",
            at = @At("TAIL")
    )
    private void onWriteNetData(FriendlyByteBuf buffer, CallbackInfo ci) {
        buffer.writeVarInt(this.stellar$dependencies.size());
        for(QuestObject dependency : this.stellar$dependencies) {
            buffer.writeLong(dependency.isValid() ? dependency.id : 0L);
        }
//        buffer.writeCollection(this.stellar$dependencies, (buf, dependency) -> buf.writeLong(dependency.isValid() ? dependency.id : 0L));
        DependencyRequirement.NAME_MAP.write(buffer, this.stellar$dependencyRequirement);
        buffer.writeBoolean(this.stellar$invisibleUntilDependenciesCompleted);
        buffer.writeBoolean(this.stellar$lockUntilDependenciesCompleted);
    }

    @Inject(
            method = "readNetData",
            at = @At("TAIL")
    )
    private void onReadNetData(FriendlyByteBuf buffer, CallbackInfo ci) {
        int dependenciesSize = buffer.readVarInt();
        this.stellar$clearDependencies();
        for(int i = 0; i < dependenciesSize; ++i) {
            QuestObject questObject = this.file.get(buffer.readLong());
            if (questObject != null) {
                this.stellar$addDependency(questObject);
            }
        }
//        buffer.readCollection(buf -> {
//            QuestObject questObject = this.file.get(buf);
//            if (questObject != null) {
//                this.stellar$addDependency(questObject);
//            }
//            return null;
//        }, FriendlyByteBuf::readLong);

        this.stellar$dependencyRequirement = DependencyRequirement.NAME_MAP.read(buffer);
        this.stellar$invisibleUntilDependenciesCompleted = buffer.readBoolean();
        this.stellar$lockUntilDependenciesCompleted = buffer.readBoolean();
    }
}
