package cn.academy.block.tileentity;

import cn.academy.energy.api.IFItemManager;
import cn.academy.energy.api.block.IWirelessGenerator;
import cn.lambdalib2.s11n.network.NetworkMessage;
import cn.lambdalib2.s11n.network.NetworkMessage.Listener;
import cn.lambdalib2.s11n.network.TargetPoints;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraftforge.fml.relauncher.Side;

/**
 * @author WeAthFolD
 */
public abstract class TileGeneratorBase extends TileInventory implements IWirelessGenerator, ITickable
{
    
    public final double bufferSize;
    public final double bandwidth;
    
    private int updateTicker = 20;
    
    /**
     * Amount of buffered energy.
     */
    private double energy;

    public TileGeneratorBase(String _invName, int size, double _bufferSize, double _bandwidth) {
        super(_invName, size);
        bufferSize = _bufferSize;
        bandwidth = _bandwidth;
    }

    @Override
    public void update() {
        if(!getWorld().isRemote) {
            double required = bufferSize - energy;
            energy += getGeneration(required);
            if (energy > bufferSize)
                energy = bufferSize;
            
            if(--updateTicker == 0) {
                updateTicker = 20;
                NetworkMessage.sendToAllAround(TargetPoints.convert(this, 20), this, "sync_energy", energy);
            }
        }
    }
    
    /**
     * Manually add [amt] energy into the buffer.
     * @return Energy not consumed
     */
    public double addEnergy(double amt) {
        return addEnergy(amt, false);
    }
    
    public double addEnergy(double amt, boolean simulate) {
        double add = Math.min(bufferSize - energy, amt);
        if(!simulate)
            energy += add;
        return amt - add;
    }

    @Override
    public double getProvidedEnergy(double req) {
        if(req > energy) req = energy;
        
        energy -= req;
        return req;
    }
    
    public double getEnergy() {
        return energy;
    }
    
    public void setEnergy(double energy) {
        this.energy = energy;
    }

    @Override
    public double getBandwidth() {
        return bandwidth;
    }
    
    /**
     * Try to charge a ItemStack with the buffer energy within the generator.
     */
    public void tryChargeStack(ItemStack stack) {
        if(IFItemManager.instance.isSupported(stack)) {
            double cangive = Math.min(energy, bandwidth);
            double ret = IFItemManager.instance.charge(stack, cangive);
            energy -= (cangive - ret);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        energy = tag.getDouble("energy");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setDouble("energy", energy);
        return tag;
    }
    
    /**
     * Get the energy generated by the generator this tick.
     */
    public abstract double getGeneration(double required);

    @Listener(channel="sync_energy", side=Side.CLIENT)
    private void hSync(double energy) {
        this.energy = energy;
    }

}