package com.luolian.stellarmod.server.listener;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class NBTStructurePlacer {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Nullable
    public static StructureTemplate loadStructureFromFile(ServerLevel level, Path filePath) {
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            CompoundTag tag = tryReadCompressed(inputStream);
            HolderGetter<Block> blockGetter = level.holderLookup(BuiltInRegistries.BLOCK.key());

            StructureTemplate template = new StructureTemplate();
            template.load(blockGetter, tag);

            LOGGER.info("成功加载结构文件: {}", filePath);
            return template;
        } catch (Exception e) {
            LOGGER.error("无法加载结构文件: {}", filePath, e);
            return null;
        }
    }

    private static CompoundTag tryReadCompressed(InputStream inputStream) throws IOException {
        BufferedInputStream bufferedStream = new BufferedInputStream(inputStream);
        bufferedStream.mark(2);

        int magic = bufferedStream.read() | (bufferedStream.read() << 8);
        bufferedStream.reset();

        if (magic == 0x8B1F) {
            return NbtIo.readCompressed(bufferedStream);
        } else {
            try (DataInputStream dataStream = new DataInputStream(bufferedStream)) {
                return NbtIo.read(dataStream);
            }
        }
    }

    public static boolean placeStructure(ServerLevel level, BlockPos pos, StructureTemplate template) {
        if (template == null || level == null) {
            return false;
        }

        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setMirror(Mirror.NONE)
                .setRotation(Rotation.NONE)
                .setIgnoreEntities(false);

        template.placeInWorld(level, pos, pos, settings, level.getRandom(), 2);

        LOGGER.info("结构已生成于: {} {} {}", pos.getX(), pos.getY(), pos.getZ());
        return true;
    }
}