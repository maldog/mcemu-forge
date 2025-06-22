package net.mcemu.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import net.mcemu.RomManager;

import java.util.List;

public class CartridgeItem extends Item {

    public CartridgeItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static String getRomFilename(ItemStack stack) {
        if (!stack.hasTag()) return "missing.nes";

        int cmd = stack.getTag().getInt("CustomModelData");
        if (cmd > 0 && cmd <= RomManager.ROM_FILE_NAMES.size()) {
            return RomManager.ROM_FILE_NAMES.get(cmd - 1);
        }

        System.err.println("❌ Invalid CMD: " + cmd + ", ROM list size: " + RomManager.ROM_FILE_NAMES.size());
        return "missing.nes";
    }



    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        if (stack.hasTag() && stack.getTag().contains("CustomModelData")) {
            int cmd = stack.getTag().getInt("CustomModelData");
            if (cmd > 0 && cmd <= RomManager.ROM_FILE_NAMES.size()) {
                String romName = RomManager.ROM_FILE_NAMES.get(cmd - 1);
                tooltip.add(Component.literal("ROM: " + romName));
            }
        }
    }
}
