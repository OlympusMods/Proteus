package com.github.olympus.proteus.client;

import com.github.olympus.proteus.client.ui.Glass;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.github.olympus.proteus.mixin.GuiRendererAccess;
import com.github.olympus.proteus.mixin.GuiRendererDrawAccess;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

public final class GlassRenderer {
    private static final int MAX_PER_FRAME = 256;
    private static final int FLOATS = 24;
    private static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(ProteusClient.id("pipeline/glass"))
            .withVertexShader(ProteusClient.id("core/glass"))
            .withFragmentShader(ProteusClient.id("core/glass"))
            .withUniform("GlassInfo", UniformType.UNIFORM_BUFFER)
            .build();

    private static final List<GpuTextureView> VIEWS = new ArrayList<>();
    private static final List<GpuBuffer> BUFFERS = new ArrayList<>();
    private static final List<float[]> PARAMS = new ArrayList<>();
    private static final Map<GpuTextureView, Integer> SLOTS = new IdentityHashMap<>();

    private static boolean failed;
    private static boolean ready;
    private static boolean inside;
    private static int count;
    private static GpuTexture texture;
    private static GpuSampler sampler;
    private static int textureWidth;
    private static int textureHeight;

    private static GpuBuffer pending;

    private GlassRenderer() {
    }

    static void reload() {
        ready = false;
        failed = false;
    }

    static boolean submit(GuiGraphics graphics, int x, int y, int w, int h, Glass glass, int guiWidth, int guiHeight) {
        if (failed || count >= MAX_PER_FRAME) {
            return false;
        }
        try {
            if (!ready) {
                setup();
            }
            RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
            if (count == 0 && (texture == null || textureWidth != main.width || textureHeight != main.height)) {
                resize(main);
            }
            int slot = count++;
            while (VIEWS.size() <= slot) {
                GpuTextureView view = RenderSystem.getDevice().createTextureView(texture);
                Minecraft.getInstance().getTextureManager().register(id(VIEWS.size()),
                        new BackdropTexture(texture, view, sampler));
                SLOTS.put(view, VIEWS.size());
                VIEWS.add(view);
                PARAMS.add(new float[FLOATS]);
            }
            write(PARAMS.get(slot), graphics, x, y, w, h, glass, guiWidth, guiHeight);
            float hw = w / 2f;
            float hh = h / 2f;
            graphics.blit(PIPELINE, id(slot), x, y, -hw, -hh, w, h, 1, 1);
            return true;
        } catch (RuntimeException exception) {
            fail(exception);
            return false;
        }
    }

    public static void bindUniforms(RenderPass pass) {
        if (pending != null) {
            pass.setUniform("GlassInfo", pending);
        }
    }

    public static void endFrame() {
        count = 0;
    }

    public static boolean drawRange(GuiRenderer renderer, Supplier<String> label, RenderTarget target,
            GpuBufferSlice fog, GpuBufferSlice transforms, GpuBuffer indices, VertexFormat.IndexType indexType,
            int start, int end) {
        if (inside || count == 0 || !ready) {
            return false;
        }
        GuiRendererAccess access = (GuiRendererAccess) renderer;
        List<?> list = access.proteus$draws();
        int last = Math.min(end, list.size());
        boolean any = false;
        for (int i = start; i < last && !any; i++) {
            any = slot(list.get(i)) >= 0;
        }
        if (!any) {
            return false;
        }
        inside = true;
        try {
            int from = start;
            for (int i = start; i < last; i++) {
                int slot = slot(list.get(i));
                if (slot < 0) {
                    continue;
                }
                if (i > from) {
                    access.proteus$executeDrawRange(label, target, fog, transforms, indices, indexType, from, i);
                }
                if (!failed) {
                    prepare(slot, target);
                    pending = BUFFERS.get(slot);
                    try {
                        access.proteus$executeDrawRange(label, target, fog, transforms, indices, indexType, i, i + 1);
                    } finally {
                        pending = null;
                    }
                }
                from = i + 1;
            }
            if (last > from) {
                access.proteus$executeDrawRange(label, target, fog, transforms, indices, indexType, from, last);
            }
        } catch (RuntimeException exception) {
            fail(exception);
        } finally {
            inside = false;
        }
        return true;
    }

    private static int slot(Object draw) {
        GuiRendererDrawAccess access = (GuiRendererDrawAccess) draw;
        if (access.proteus$pipeline() != PIPELINE) {
            return -1;
        }
        TextureSetup textures = access.proteus$textureSetup();
        Integer slot = textures == null ? null : SLOTS.get(textures.texure0());
        return slot == null || slot >= count ? -1 : slot;
    }

    private static void prepare(int slot, RenderTarget target) {
        GpuDevice device = RenderSystem.getDevice();
        CommandEncoder encoder = device.createCommandEncoder();
        encoder.copyTextureToTexture(target.getColorTexture(), texture, 0, 0, 0, 0, 0,
                Math.min(textureWidth, target.width), Math.min(textureHeight, target.height));
        while (BUFFERS.size() <= slot) {
            BUFFERS.add(device.createBuffer(() -> "Proteus glass", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST,
                    FLOATS * 4L));
        }
        float[] values = PARAMS.get(slot);
        values[10] = target.width;
        values[11] = target.height;
        ByteBuffer bytes = ByteBuffer.allocateDirect(FLOATS * 4).order(ByteOrder.nativeOrder());
        for (float value : values) {
            bytes.putFloat(value);
        }
        bytes.flip();
        encoder.writeToBuffer(BUFFERS.get(slot).slice(), bytes);
    }

    private static void write(float[] values, GuiGraphics graphics, int x, int y, int w, int h, Glass glass,
            int guiWidth, int guiHeight) {
        float hw = w / 2f;
        float hh = h / 2f;
        Minecraft minecraft = Minecraft.getInstance();
        Window window = minecraft.getWindow();
        float mouseX = (float) (minecraft.mouseHandler.xpos() * guiWidth / Math.max(1, window.getScreenWidth()));
        float mouseY = (float) (minecraft.mouseHandler.ypos() * guiHeight / Math.max(1, window.getScreenHeight()));
        Vector2f pointer = new Matrix3x2f(graphics.pose()).invert().transformPosition(new Vector2f(mouseX, mouseY));
        int tint = glass.tint();
        float[] packed = {
            hw, hh, glass.radius(), 0f,
            glass.refraction(), glass.depth(), glass.blur(), glass.dispersion(),
            glass.zoom(), glass.smear(), 1f, 1f,
            glass.highlight(), (float) Math.toRadians(glass.light()), glass.saturation(), glass.opacity(),
            pointer.x - x - hw, pointer.y - y - hh, glass.glow(), Math.max(16f, Math.min(w, h) * 1.5f),
            ((tint >> 16) & 0xFF) / 255f, ((tint >> 8) & 0xFF) / 255f, (tint & 0xFF) / 255f, (tint >>> 24) / 255f
        };
        System.arraycopy(packed, 0, values, 0, FLOATS);
    }

    private static void setup() {
        if (!RenderSystem.getDevice().precompilePipeline(PIPELINE).isValid()) {
            throw new IllegalStateException("the glass shader did not compile");
        }
        sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
        ready = true;
    }

    private static Identifier id(int slot) {
        return ProteusClient.id("glass/backdrop_" + slot);
    }

    private static void resize(RenderTarget main) {
        TextureManager textures = Minecraft.getInstance().getTextureManager();
        for (int slot = 0; slot < VIEWS.size(); slot++) {
            textures.release(id(slot));
        }
        VIEWS.clear();
        PARAMS.clear();
        SLOTS.clear();
        if (texture != null) {
            texture.close();
        }
        texture = RenderSystem.getDevice().createTexture("Proteus glass backdrop",
                GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING, main.getColorTexture().getFormat(),
                main.width, main.height, 1, 1);
        textureWidth = main.width;
        textureHeight = main.height;
    }

    private static void fail(Throwable problem) {
        if (!failed) {
            ProteusClient.LOGGER.warn("Proteus: glass could not be drawn, drawing it flat", problem);
        }
        failed = true;
    }

    private static final class BackdropTexture extends AbstractTexture {
        BackdropTexture(GpuTexture texture, GpuTextureView view, GpuSampler sampler) {
            this.texture = texture;
            this.textureView = view;
            this.sampler = sampler;
        }

        @Override
        public void close() {
            if (textureView != null) {
                textureView.close();
                textureView = null;
            }
            texture = null;
        }
    }
}
