package com.github.olympus.proteus.mixin;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.gui.render.GuiRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiRenderer.class)
public interface GuiRendererAccess {
    @Accessor("draws")
    List<?> proteus$draws();

    @Invoker("executeDrawRange")
    void proteus$executeDrawRange(Supplier<String> label, RenderTarget target, GpuBufferSlice fog,
            GpuBufferSlice transforms, GpuBuffer indices, VertexFormat.IndexType indexType, int start, int end);
}
