package com.github.olympus.proteus.client;

import com.github.olympus.proteus.client.ui.Glass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

final class GlassRenderer {
    private static final String PREFIX = "shaders/core/proteus_";

    private static ShaderInstance shader;
    private static boolean failed;
    private static TextureTarget copy;

    private GlassRenderer() {
    }

    static void reload() {
        RenderSystem.recordRenderCall(() -> {
            if (shader != null) {
                shader.close();
            }
            shader = null;
            failed = false;
        });
    }

    private static ShaderInstance shader() {
        if (shader != null || failed) {
            return shader;
        }
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        ResourceProvider provider = location -> {
            String path = location.getPath();
            if (location.getNamespace().equals("minecraft") && path.startsWith(PREFIX)) {
                return manager.getResource(ProteusClient.id("shaders/core/" + path.substring(PREFIX.length())));
            }
            return manager.getResource(location);
        };
        try {
            shader = new ShaderInstance(provider, "proteus_glass", DefaultVertexFormat.POSITION_TEX_COLOR);
        } catch (Exception exception) {
            failed = true;
            ProteusClient.LOGGER.warn("Proteus: could not load the glass shader, drawing glass flat", exception);
        }
        return shader;
    }

    private static RenderTarget target(RenderTarget main, int bound) {
        if (copy != null && copy.width == main.width && copy.height == main.height) {
            return copy;
        }
        if (copy == null) {
            copy = new TextureTarget(main.width, main.height, false, Minecraft.ON_OSX);
        } else {
            copy.resize(main.width, main.height, Minecraft.ON_OSX);
        }
        copy.setFilterMode(GL11.GL_LINEAR);
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, bound);
        GlStateManager._viewport(0, 0, main.viewWidth, main.viewHeight);
        return copy;
    }

    static boolean draw(GuiGraphics graphics, int x, int y, int w, int h, Glass glass) {
        ShaderInstance program = shader();
        if (program == null) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Window window = minecraft.getWindow();
        RenderTarget main = minecraft.getMainRenderTarget();
        Matrix4f pose = graphics.pose().last().pose();

        float margin = (Math.abs(glass.refraction()) + Math.abs(glass.zoom())) * (1f + glass.dispersion()) + glass.blur()
                + glass.smear() + 2f;
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        Vector4f corner = new Vector4f();
        for (int i = 0; i < 4; i++) {
            float cx = (i & 1) == 0 ? x - margin : x + w + margin;
            float cy = (i & 2) == 0 ? y - margin : y + h + margin;
            pose.transform(corner.set(cx, cy, 0f, 1f));
            minX = Math.min(minX, corner.x);
            minY = Math.min(minY, corner.y);
            maxX = Math.max(maxX, corner.x);
            maxY = Math.max(maxY, corner.y);
        }
        double scale = window.getGuiScale();
        int x0 = clamp((int) Math.floor(minX * scale), main.width);
        int x1 = clamp((int) Math.ceil(maxX * scale), main.width);
        int y0 = clamp(main.height - (int) Math.ceil(maxY * scale), main.height);
        int y1 = clamp(main.height - (int) Math.floor(minY * scale), main.height);
        if (x1 <= x0 || y1 <= y0) {
            return true;
        }

        graphics.flush();
        int bound = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        RenderTarget backdrop = target(main, bound);
        boolean scissor = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        if (scissor) {
            GlStateManager._disableScissorTest();
        }
        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, bound);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, backdrop.frameBufferId);
        GlStateManager._glBlitFrameBuffer(x0, y0, x1, y1, x0, y0, x1, y1, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, bound);
        if (scissor) {
            GlStateManager._enableScissorTest();
        }

        float hw = w / 2f;
        float hh = h / 2f;
        float pointerX = (float) (minecraft.mouseHandler.xpos() * main.width / Math.max(1, window.getScreenWidth()));
        float pointerY = main.height
                - (float) (minecraft.mouseHandler.ypos() * main.height / Math.max(1, window.getScreenHeight()));
        program.safeGetUniform("HalfSize").set(hw, hh);
        program.safeGetUniform("Radius").set((float) glass.radius());
        program.safeGetUniform("Optics").set(glass.refraction(), glass.depth(), glass.blur(), glass.dispersion());
        program.safeGetUniform("Lens").set(glass.zoom(), glass.smear(), 0f, 0f);
        program.safeGetUniform("Light").set(glass.highlight(), (float) Math.toRadians(glass.light()),
                glass.saturation(), glass.opacity());
        program.safeGetUniform("Glow").set(pointerX, pointerY, glass.glow(),
                (float) (Math.max(16f, Math.min(w, h) * 1.5f) * scale));
        program.safeGetUniform("CopyRect").set((float) x0, (float) y0, (float) x1, (float) y1);

        RenderSystem.setShader(() -> program);
        RenderSystem.setShaderTexture(0, backdrop.getColorTextureId());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        int tint = glass.tint();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.addVertex(pose, x, y, 0f).setUv(-hw, -hh).setColor(tint);
        buffer.addVertex(pose, x, y + h, 0f).setUv(-hw, hh).setColor(tint);
        buffer.addVertex(pose, x + w, y + h, 0f).setUv(hw, hh).setColor(tint);
        buffer.addVertex(pose, x + w, y, 0f).setUv(hw, -hh).setColor(tint);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        return true;
    }

    private static int clamp(int value, int max) {
        return Math.max(0, Math.min(max, value));
    }
}
