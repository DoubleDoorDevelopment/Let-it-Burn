package net.doubledoordev.letitburn.letitburn.client;

import net.doubledoordev.letitburn.letitburn.LetItBurn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Dries007
 */
@SideOnly(Side.CLIENT)
@SuppressWarnings("unused")
public class ConfigGuiFactory implements IModGuiFactory
{
    @SuppressWarnings("WeakerAccess")
    public static class ConfigGuiScreen extends GuiConfig
    {
        public ConfigGuiScreen(GuiScreen parentScreen)
        {
            super(parentScreen, getConfigElements(), LetItBurn.MOD_ID, false, false, LetItBurn.MOD_NAME);
        }

        private static List<IConfigElement> getConfigElements()
        {
            Configuration c = LetItBurn.getConfig();
            if (c.getCategoryNames().size() == 1)
            {
                //noinspection LoopStatementThatDoesntLoop
                for (String k : c.getCategoryNames())
                {
                    // Let forge do the work, for loop abused to avoid other strange constructs
                    return new ConfigElement(c.getCategory(k)).getChildElements();
                }
            }

            List<IConfigElement> list = new ArrayList<>();
            for (String k : c.getCategoryNames())
            {
                list.add(new ConfigElement(c.getCategory(k)));
            }
            return list;
        }
    }

    @Override
    public void initialize(Minecraft minecraftInstance)
    {

    }

    @Override
    public boolean hasConfigGui()
    {
        return true;
    }

    @Override
    public GuiScreen createConfigGui(GuiScreen parentScreen)
    {
        return new ConfigGuiScreen(parentScreen);
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories()
    {
        return null;
    }

}
