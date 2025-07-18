package net.crsimple.airplaneslogger.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import immersive_aircraft.item.VehicleItem;
import net.crsimple.airplaneslogger.util.OwnerItemData;
import net.luckperms.api.LuckPermsProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class OwnerCommand {

    public static final String SET_PERM = "airplanes_logger.owner.set";
    public static final String RESET_PERM = "airplanes_logger.owner.reset";
    public static final String INFO_PERM = "airplanes_logger.owner.info";
    public static final String GENERAL_PERM = "airplanes_logger.owner";

    public static void reg(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("owner")
                .then(Commands.literal("info")
                    .executes(OwnerCommand::executeInfo))
                .then(Commands.literal("set")
                        .then(Commands.argument("player_name", StringArgumentType.string())
                                .suggests(new PlayerSuggestionProvider())
                                .executes(OwnerCommand::executeSet)))
                .requires(source -> source.hasPermission(4) ||
                        checkAnyPerm(source.getPlayer()))
        );
    }

    private static boolean checkAnyPerm(ServerPlayer player) {
        return checkPermission(player,GENERAL_PERM) || checkPermission(player,INFO_PERM) ||
                checkPermission(player,SET_PERM) || checkPermission(player,RESET_PERM);
    }

    private static int executeInfo(CommandContext<CommandSourceStack> cmd) {
        if(cmd.getSource().getEntity() instanceof Player player) {
            if(checkPermission(player, INFO_PERM)) {
                sendOwnerInfoToPlayer(player);
            } else {
                error(player,"command.airplanes_logger.owner.no_enough_perms");
            }
        }
        return Command.SINGLE_SUCCESS;
    }
    private static int executeSet(CommandContext<CommandSourceStack> cmd) {
        if(cmd.getSource().getEntity() instanceof ServerPlayer player) {
            if(checkPermission(player,SET_PERM) || checkPermission(player,RESET_PERM)) {
                String arg = cmd.getArgument("player_name", String.class);
                setOwnerInfo(player, arg);
            } else {
                error(player,"command.airplanes_logger.owner.no_enough_perms");
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private static void error(Player player, String key, Object... args) {
        player.sendSystemMessage(Component.translatable(key,args)
                .withColor(0x8B0000));
    }
    private static void setOwnerInfo(Player player, String arg) {
        ItemStack plane = getPlaneItem(player);
        if(plane == null) {
            error(player,"command.airplanes_logger.owner.plane_item_not_found");
            return;
        }

        if(((OwnerItemData)plane.getItem()).getOwner(plane) != null && !checkPermission(player,RESET_PERM)) {
            error(player,"command.airplanes_logger.owner.no_enough_perms");
            return;
        }

        ((OwnerItemData)plane.getItem()).setOwner(plane,arg);
        player.sendSystemMessage(Component.translatable("command.airplanes_logger.owner.set",
                ((OwnerItemData)plane.getItem()).getOwner(plane)).withColor(0xFFFF00));
    }

    private static void sendOwnerInfoToPlayer(Player player) {
        ItemStack plane = getPlaneItem(player);
        if(plane == null) {
            error(player,"command.airplanes_logger.owner.plane_item_not_found");
            return;
        }

        if(((OwnerItemData)plane.getItem()).getOwner(plane) == null) {
            player.sendSystemMessage(Component.translatable("command.airplanes_logger.owner.none_info")
                    .withColor(0xFFFF00));
            return;
        }
        player.sendSystemMessage(Component.translatable("command.airplanes_logger.owner.info",
                ((OwnerItemData)plane.getItem()).getOwner(plane)).withColor(0xFFFF00));

    }

    @lombok.SneakyThrows
    private static boolean checkPermission(Player player, String perm) {
        if (player == null) {
            return false;
        }
        if(player.hasPermissions(4)) {
            return true;
        }
        var user = LuckPermsProvider.get().getUserManager().getUser(player.getUUID());
        if(user == null) {
            user = LuckPermsProvider.get().getUserManager().loadUser(player.getUUID()).get();
        }
//        var options = LuckPermsProvider.get().getContextManager().getQueryOptions(user).orElse(
//                LuckPermsProvider.get().getContextManager().getStaticQueryOptions());

        return user.getCachedData().getPermissionData().checkPermission(perm).asBoolean();
    }

    private static @Nullable ItemStack getPlaneItem(Player player) {
        return player.getMainHandItem().getItem() instanceof VehicleItem? player.getMainHandItem():
                player.getOffhandItem().getItem() instanceof VehicleItem? player.getOffhandItem():null;
    }
}
