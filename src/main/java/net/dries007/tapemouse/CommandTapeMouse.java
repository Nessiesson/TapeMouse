package net.dries007.tapemouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.input.Keyboard.KEY_NONE;

/**
 * @author Dries007
 */
@SideOnly(Side.CLIENT)
public class CommandTapeMouse extends CommandBase
{
    private static final List<KeyBinding> KEYBIND_ARRAY = ReflectionHelper.getPrivateValue(KeyBinding.class, null, "keybindArray", "field_74516_a");
    private boolean prevPauseSetting = true; // defaults to true

    @Override
    public String getCommandName()
    {
        return TapeMouse.MODID.toLowerCase();
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return sender instanceof EntityPlayer;
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return '/' + getCommandName() + " [off|keybinding name ...] [delay] OR Use no arguments to get a list of keybindings.";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length == 0)
        {
            sender.addChatMessage(new ChatComponentText("List of keybindings").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA)));
            sender.addChatMessage(new ChatComponentText("Key => NAME (category)").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA)));

            for (KeyBinding keyBinding : KEYBIND_ARRAY)
            {
                if (keyBinding == null || keyBinding.getKeyCode() == KEY_NONE) continue;
                String name = keyBinding.getKeyDescription();
                if (name == null) continue;
                name = name.replaceFirst("^key\\.", "");

                String cat = keyBinding.getKeyCategory();
                if (cat == null) continue;
                cat = cat.replaceFirst("^key\\.", "");
                cat = cat.replaceFirst("^categories\\.", "");

                sender.addChatMessage(new ChatComponentText(keyBinding.getKeyDescription() + " => " + name + " (" + cat + ")"));
            }
            return;
        }
        if (args[0].equalsIgnoreCase("off"))
        {
            Minecraft.getMinecraft().gameSettings.pauseOnLostFocus = prevPauseSetting;
            TapeMouse.keyBinding = null;
            TapeMouse.i = 0;
            sender.addChatMessage(new ChatComponentText("TapeMouse off."));
        }
        else
        {
            StringBuilder askedName = new StringBuilder(args[0]);
            if (args.length > 1)
            {
                try
                {
                    TapeMouse.delay = parseInt(args[args.length - 1], 0);
                    for (int i = 1; i < args.length - 1; i++) askedName.append(' ').append(args[i]);
                }
                catch (NumberInvalidException e)
                {
                    // Assume last part was not a number
                    for (int i = 1; i < args.length; i++) askedName.append(' ').append(args[i]);
                }
                askedName = new StringBuilder(askedName.toString().trim());
            }
            for (KeyBinding keyBinding : KEYBIND_ARRAY)
            {
                if (keyBinding == null) continue;
                String name = keyBinding.getKeyDescription();
                //noinspection ConstantConditions Because of stupid mod authors...
                if (name == null) continue;
                name = name.replaceFirst("^key\\.", "");
                if (askedName.toString().equalsIgnoreCase(name))
                {
                    prevPauseSetting = Minecraft.getMinecraft().gameSettings.pauseOnLostFocus;
                    Minecraft.getMinecraft().gameSettings.pauseOnLostFocus = false;
                    TapeMouse.keyBinding = keyBinding;
                    sender.addChatMessage(new ChatComponentText("TapeMouse on '" + name + "' (" + keyBinding.getKeyDescription() + ") with delay " + TapeMouse.delay + " ticks."));
                    return;
                }
            }

            throw new CommandException(askedName + " is unknown keybinding. If your keybind ends in a number, you *have* to specify the delay.");
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1)
        {
            List<String> list = new ArrayList<>();
            list.add("off");
            for (KeyBinding keyBinding : KEYBIND_ARRAY)
            {
                if (keyBinding == null || keyBinding.getKeyCode() == KEY_NONE) continue;
                String name = keyBinding.getKeyDescription();
                //noinspection ConstantConditions Because of stupid mod authors...
                if (name == null) continue;
                name = name.replaceFirst("^key\\.", "");
                list.add(name);
            }
            return getListOfStringsMatchingLastWord(args, list);
        }
        return super.addTabCompletionOptions( sender, args, pos);
    }
}
