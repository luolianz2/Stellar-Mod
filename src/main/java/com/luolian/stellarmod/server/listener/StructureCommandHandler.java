package com.luolian.stellarmod.server.listener;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;

@Mod.EventBusSubscriber(modid = "stellarmod", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class StructureCommandHandler {

    private static final Path STRUCTURES_DIR = FMLPaths.CONFIGDIR.get().resolve("structures");

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        System.out.println("[StellarMod] 正在注册 /placenbt 命令...");

        try {
            if (!Files.exists(STRUCTURES_DIR)) {
                Files.createDirectories(STRUCTURES_DIR);
                System.out.println("[StellarMod] 已创建目录: " + STRUCTURES_DIR);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("placenbt")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("fileName", StringArgumentType.string())
                                .executes(ctx -> placeAtPlayer(ctx))
                                .then(Commands.argument("pos", net.minecraft.commands.arguments.coordinates.BlockPosArgument.blockPos())
                                        .executes(ctx -> placeAtPos(ctx))
                                )
                        )
                        .then(Commands.literal("list")
                                .executes(ctx -> listStructures(ctx))
                        )
        );

        System.out.println("[StellarMod] /placenbt 命令注册完成");
    }

    private static int placeAtPlayer(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        String fileName = StringArgumentType.getString(ctx, "fileName");
        BlockPos pos = BlockPos.containing(ctx.getSource().getPosition());
        return place(ctx, fileName, pos);
    }

    private static int placeAtPos(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        String fileName = StringArgumentType.getString(ctx, "fileName");

        try {
            BlockPos pos = net.minecraft.commands.arguments.coordinates.BlockPosArgument.getLoadedBlockPos(ctx, "pos");
            return place(ctx, fileName, pos);
        } catch (CommandSyntaxException e) {
            ctx.getSource().sendFailure(Component.literal("§c无效的坐标: " + e.getMessage()));
            return 0;
        }
    }

    private static int place(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx,
                             String fileName, BlockPos pos) {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();

        final String finalFileName = fileName.endsWith(".nbt") ? fileName : fileName + ".nbt";
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();

        Path filePath = STRUCTURES_DIR.resolve(finalFileName);

        source.sendSuccess(() -> Component.literal("§7正在加载: " + finalFileName), false);

        if (!Files.exists(filePath)) {
            source.sendFailure(Component.literal(
                    "§c文件不存在: " + finalFileName + "\n" +
                            "§7请将文件放入: " + STRUCTURES_DIR
            ));
            return 0;
        }

        StructureTemplate template = NBTStructurePlacer.loadStructureFromFile(level, filePath);

        if (template == null) {
            source.sendFailure(Component.literal("§c无法加载结构文件: " + finalFileName));
            return 0;
        }

        boolean success = NBTStructurePlacer.placeStructure(level, pos, template);

        if (success) {
            Component message = Component.literal(
                    String.format("§a结构 %s 已生成于: %d %d %d", finalFileName, x, y, z)
            );
            source.sendSuccess(() -> message, true);
            return 1;
        } else {
            source.sendFailure(Component.literal("§c结构放置失败"));
            return 0;
        }
    }

    private static int listStructures(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();

        try {
            if (!Files.exists(STRUCTURES_DIR)) {
                source.sendFailure(Component.literal("§c目录不存在: " + STRUCTURES_DIR));
                return 0;
            }

            StringBuilder sb = new StringBuilder("§e可用的结构文件:\n");
            boolean hasFiles = false;

            var files = Files.list(STRUCTURES_DIR).iterator();
            while (files.hasNext()) {
                Path p = files.next();
                if (p.toString().endsWith(".nbt")) {
                    sb.append("§7  - ").append(p.getFileName()).append("\n");
                    hasFiles = true;
                }
            }

            if (!hasFiles) {
                sb.append("§7  (没有找到 .nbt 文件)\n");
                sb.append("§7  请将文件放入: ").append(STRUCTURES_DIR);
            }

            final String message = sb.toString();
            source.sendSuccess(() -> Component.literal(message), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§c无法列出文件: " + e.getMessage()));
            return 0;
        }
    }
}