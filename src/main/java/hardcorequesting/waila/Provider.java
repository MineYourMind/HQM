package hardcorequesting.waila;


import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hardcorequesting.blocks.BlockDelivery;
import hardcorequesting.blocks.BlockPortal;
import hardcorequesting.blocks.ModBlocks;
import hardcorequesting.client.interfaces.GuiColor;
import hardcorequesting.quests.QuestTask;
import hardcorequesting.tileentity.PortalType;
import hardcorequesting.tileentity.TileEntityBarrel;
import hardcorequesting.tileentity.TileEntityPortal;
import mcp.mobius.waila.api.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;


import java.util.ArrayList;
import java.util.List;

public class Provider implements IWailaDataProvider {


    private static final String MOD_NAME = "HQM";
    private static final String IS_REMOTE_AVAILABLE = MOD_NAME + ".showQDS";

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (accessor.getBlock() == ModBlocks.itemPortal) {
            TileEntity te = accessor.getTileEntity();
            if (te instanceof TileEntityPortal) {
                TileEntityPortal portal = (TileEntityPortal)te;
                if (portal.hasTexture(getPlayer())) {
                    if (portal.getType().isPreset()) {
                        return new ItemStack(ModBlocks.itemPortal, 1, portal.getType() == PortalType.TECH ? 1 : 2);
                    }else{
                        return portal.getItem();
                    }
                }else{
                    return new ItemStack((Block)null);
                }
            }
        }

        return null;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {

        return currenttip;
    }

    @SideOnly(Side.CLIENT)
    private EntityPlayer getPlayer() {
        return Minecraft.getMinecraft().thePlayer;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (config.getConfig(IS_REMOTE_AVAILABLE)) {
            if (itemStack != null && itemStack.getItem() == Item.getItemFromBlock(accessor.getBlock())) {
                TileEntity te = accessor.getTileEntity();
                if (te != null ) {
                    if (te instanceof TileEntityBarrel) {
                        TileEntityBarrel qds = (TileEntityBarrel)te;

                        qds.readFromNBT(accessor.getNBTData());

                        QuestTask task = qds.getCurrentTask();
                        if(task != null && te.getBlockMetadata() == 1) {
                            currenttip.add(qds.getPlayer());
                            currenttip.add(task.getParent().getName());
                            currenttip.add(task.getDescription());
                            currenttip.add((int)(task.getCompletedRatio(qds.getPlayer()) * 100) + "% completed");
                        }
                    }
                }
            }
        }


        return currenttip;
    }

    @SideOnly(Side.CLIENT)
    private boolean isShiftDown() {
        return GuiScreen.isShiftKeyDown();
    }


    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if(itemStack.getItem() == Item.getItemFromBlock(ModBlocks.itemPortal) && itemStack.getItemDamage() > 0) {
            //currenttip =  new ArrayList<String>();
            //currenttip.add(GuiColor.LIGHT_BLUE + "Minecraft");
        }

        return currenttip;
    }

    public static void callbackRegister(IWailaRegistrar registrar) {
        Provider instance = new Provider();
        registrar.registerStackProvider(instance, BlockPortal.class);


        registrar.registerBodyProvider(instance, BlockDelivery.class);
        registrar.registerSyncedNBTKey("*", BlockDelivery.class);
        registrar.addConfigRemote(MOD_NAME, IS_REMOTE_AVAILABLE, "Show QDS data");
    }
}
