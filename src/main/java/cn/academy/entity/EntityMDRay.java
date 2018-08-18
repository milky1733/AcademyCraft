package cn.academy.entity;

import cn.academy.client.render.entity.ray.RendererRayComposite;
import cn.academy.client.render.particle.MdParticleFactory;
import cn.lambdalib2.registry.mc.RegEntity;
import cn.lambdalib2.util.RandUtils;
import cn.lambdalib2.util.VecUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author WeAthFolD
 */
@SideOnly(Side.CLIENT)
@RegEntity
public class EntityMDRay extends EntityRayBase {

    public static MDRayRender renderer;


    public EntityMDRay(EntityPlayer spawner, double length) {
        super(spawner);

        Vec3d start = spawner.getPositionVector(),
                end = VecUtils.add(spawner.getPositionVector(), VecUtils.multiply(spawner.getLookVec(),length));
        this.setFromTo(start, end);
        this.blendInTime = 200;
        this.blendOutTime = 700;
        this.life = 50;
        this.length = length;
    }
    
    @Override
    public void onUpdate() {
        super.onUpdate();
        if(RandUtils.nextDouble() < 0.8) {
            Particle p = MdParticleFactory.INSTANCE.next(world,
                    new Motion3D(this, true).move(RandUtils.ranged(0, 10)).getPosVec(),
                    new Vec3d(RandUtils.ranged(-.03, .03), RandUtils.ranged(-.03, .03), RandUtils.ranged(-.03, .03)));
            world.spawnEntity(p);
        }
    }
    
    public static class MDRayRender extends RendererRayComposite {

        public MDRayRender() {
        super("mdray");
            this.cylinderIn.width = 0.17;
            this.cylinderIn.color.setColor4i(216, 248, 216, 230);
            
            this.cylinderOut.width = 0.22;
            this.cylinderOut.color.setColor4i(106, 242, 106, 50);
            
            this.glow.width = 1.5;
            this.glow.color.a = 0.8;
        }
        
    }
}