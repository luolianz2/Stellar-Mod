package com.luolian.stellarmod.api.compat;

import com.luolian.stellarmod.api.util.OriginsUtil;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.AbstractBooleanTask;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import io.github.edwinmindcraft.origins.api.OriginsAPI;
import io.github.edwinmindcraft.origins.api.capabilities.IOriginContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;

import java.util.concurrent.atomic.AtomicBoolean;

public class OriginsTask extends AbstractBooleanTask {
    private static final ResourceLocation DEFAULT_ORIGIN = OriginsUtil.location("human");
    private ResourceLocation origin = DEFAULT_ORIGIN;

    public OriginsTask(long id, Quest quest) {
        super(id, quest);
    }

    @Override
    public boolean canSubmit(TeamData teamData, ServerPlayer serverPlayer) {
        if (!OriginsUtil.isValidOrigin(this.origin)) {
            // 如果指定的起源无效，无法完成任务
            return false;
        }

        if (OriginsUtil.isEmptyOrigin(this.origin)) {
            // 如果指定为空，任意起源都满足条件
            return true;
        }

        AtomicBoolean rt = new AtomicBoolean(false);
        LazyOptional<IOriginContainer> capability = serverPlayer.getCapability(OriginsAPI.ORIGIN_CONTAINER);

        capability.ifPresent(container -> {
            if (OriginsUtil.hasOrigin(container, OriginsUtil.getLayerIdByOriginId(this.origin), this.origin)) {
                rt.set(true);
            }
        });
        return rt.get();
    }

    @Override
    public TaskType getType() {
        return StellarTaskTypes.ORIGIN;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);

        config.addEnum(
                "origin",
                origin,
                v -> origin = v,
                NameMap.of(DEFAULT_ORIGIN, OriginsUtil.getOriginIds().toArray(new ResourceLocation[0]))
                        .icon(OriginsUtil::getOriginIcon)
                        .name(originId -> {
                            ResourceLocation layerId = OriginsUtil.getLayerIdByOriginId(originId);

                            if (layerId == null) {
                                return OriginsUtil.getOriginName(originId);
                            }

                            return Component.literal(OriginsUtil.getOriginLayerName(layerId).getString()).append(": ").append(OriginsUtil.getOriginName(originId));
                        })
                        .create()
        ).setNameKey("ftbquests.task.stellarmod.origin");
    }

    @Override
    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);

        nbt.putString("origin", origin.toString());
    }

    @Override
    public void readData(CompoundTag nbt) {
        super.readData(nbt);

        this.origin = ResourceLocation.tryParse(nbt.getString("origin"));
    }

    @Override
    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeUtf(origin.toString());
    }

    @Override
    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        this.origin = ResourceLocation.tryParse(buffer.readUtf());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Component getAltTitle() {
        if (OriginsUtil.isValidOrigin(this.origin)) {
            return Component.translatable("ftbquests.task.stellarmod.origin").append(": ").append(OriginsUtil.getOriginName(this.origin)).withStyle(ChatFormatting.YELLOW);
        }
        return super.getAltTitle();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Icon getAltIcon() {
        if (OriginsUtil.isValidOrigin(this.origin)) {
            return OriginsUtil.getOriginIcon(this.origin);
        }
        return super.getAltIcon();
    }

    @Override
    public int autoSubmitOnPlayerTick() {
        return 20;
    }
}
