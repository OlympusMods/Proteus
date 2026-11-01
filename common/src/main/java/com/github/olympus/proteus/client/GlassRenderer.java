package com.github.olympus.proteus.client;

import com.github.olympus.proteus.client.ui.Glass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.BindGroupLayouts;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

public final class GlassRenderer {
    private static final int MAX_PER_FRAME = 256;
    private static final int FLOATS = 24;
    private static final String RENDERPEARL = "com.mojang.renderpearl.api.";
    private static final Map<String, Method> METHODS = new HashMap<>();

    private static final List<Object> VIEWS = new ArrayList<>();
    private static final List<Object> BUFFERS = new ArrayList<>();
    private static final List<float[]> PARAMS = new ArrayList<>();
    private static final Map<Object, Integer> SLOTS = new IdentityHashMap<>();

    private static boolean failed;
    private static boolean ready;
    private static boolean inside;
    private static int count;
    private static boolean renderpearl;
    private static Object pipeline;
    private static Object sampler;
    private static Object texture;
    private static int textureWidth;
    private static int textureHeight;

    private static Field draws;
    private static Method executeDrawRange;
    private static Method executeDraw;
    private static Constructor<?> drawConstructor;
    private static Method drawTextures;
    private static RecordComponent[] drawParts;

    private GlassRenderer() {
    }

    static void reload() {
        ready = false;
        failed = false;
    }

    static boolean submit(GuiGraphicsExtractor graphics, int x, int y, int w, int h, Glass glass, int guiWidth,
            int guiHeight) {
        if (failed || count >= MAX_PER_FRAME) {
            return false;
        }
        try {
            if (!ready) {
                setup();
            }
            RenderTarget main = Minecraft.getInstance().gameRenderer.mainRenderTarget();
            if (count == 0 && (texture == null || textureWidth != main.width || textureHeight != main.height)) {
                resize(main);
            }
            int slot = count++;
            while (VIEWS.size() <= slot) {
                Object view = call(device(), "createTextureView", texture);
                SLOTS.put(view, VIEWS.size());
                VIEWS.add(view);
                PARAMS.add(new float[FLOATS]);
            }
            write(PARAMS.get(slot), graphics, x, y, w, h, glass, guiWidth, guiHeight);
            float hw = w / 2f;
            float hh = h / 2f;
            call(graphics, "blit", VIEWS.get(slot), sampler, x, y, x + w, y + h, -hw, hw, -hh, hh);
            return true;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            fail(exception);
            return false;
        }
    }

    public static void endFrame() {
        count = 0;
    }

    public static boolean drawRange(Object renderer, Supplier<String> label, RenderTarget target, Object transforms,
            int start, int end) {
        if (inside || count == 0 || draws == null) {
            return false;
        }
        try {
            List<?> list = (List<?>) draws.get(renderer);
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
                    Object draw = list.get(i);
                    int slot = slot(draw);
                    if (slot < 0) {
                        continue;
                    }
                    if (i > from) {
                        executeDrawRange.invoke(renderer, label, target, transforms, from, i);
                    }
                    if (!failed) {
                        drawGlass(renderer, draw, slot, label, target, transforms);
                    }
                    from = i + 1;
                }
                if (last > from) {
                    executeDrawRange.invoke(renderer, label, target, transforms, from, last);
                }
            } finally {
                inside = false;
            }
            return true;
        } catch (InvocationTargetException exception) {
            fail(exception.getCause());
            return true;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            fail(exception);
            return true;
        }
    }

    private static int slot(Object draw) throws ReflectiveOperationException {
        Object textures = drawTextures.invoke(draw);
        if (textures == null) {
            return -1;
        }
        Integer slot = SLOTS.get(call(textures, "texure0"));
        return slot == null || slot >= count ? -1 : slot;
    }

    private static void drawGlass(Object renderer, Object draw, int slot, Supplier<String> label, RenderTarget target,
            Object transforms) throws ReflectiveOperationException {
        Object encoder = call(device(), "createCommandEncoder");
        call(encoder, "copyTextureToTexture", call(target, "getColorTexture"), texture, 0, 0, 0, 0, 0,
                Math.min(textureWidth, target.width), Math.min(textureHeight, target.height));
        while (BUFFERS.size() <= slot) {
            int usage = constant("buffers.GpuBuffer", "com.mojang.blaze3d.buffers.GpuBuffer", "USAGE_UNIFORM")
                    | constant("buffers.GpuBuffer", "com.mojang.blaze3d.buffers.GpuBuffer", "USAGE_COPY_DST");
            Supplier<String> name = () -> "Proteus glass";
            BUFFERS.add(call(device(), "createBuffer", name, usage, (long) FLOATS * 4L));
        }
        float[] values = PARAMS.get(slot);
        values[10] = target.width;
        values[11] = target.height;
        ByteBuffer bytes = ByteBuffer.allocateDirect(FLOATS * 4).order(ByteOrder.nativeOrder());
        for (float value : values) {
            bytes.putFloat(value);
        }
        bytes.flip();
        Object buffer = BUFFERS.get(slot);
        call(encoder, "writeToBuffer", call(buffer, "slice"), bytes);

        Object[] parts = new Object[drawParts.length];
        for (int i = 0; i < parts.length; i++) {
            parts[i] = drawParts[i].getName().equals("pipeline") ? pipeline : drawParts[i].getAccessor().invoke(draw);
        }
        Object glassDraw = drawConstructor.newInstance(parts);
        Object pass = call(encoder, "createRenderPass", label, call(target, "getColorTextureView"), Optional.empty(),
                call(target, "getDepthTextureView"), OptionalDouble.empty());
        try {
            call(RenderSystem.class, "bindDefaultUniforms", pass);
            call(pass, "setUniform", "DynamicTransforms", transforms);
            call(pass, "setUniform", "GlassInfo", buffer);
            executeDraw.invoke(renderer, glassDraw, pass);
        } finally {
            call(pass, "close");
        }
    }

    private static void write(float[] values, GuiGraphicsExtractor graphics, int x, int y, int w, int h, Glass glass,
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

    private static void setup() throws ReflectiveOperationException {
        renderpearl = present(RENDERPEARL + "pipeline.RenderPipeline");
        Class<?> renderer = GuiRenderer.class;
        draws = renderer.getDeclaredField("draws");
        draws.setAccessible(true);
        executeDrawRange = declared(renderer, "executeDrawRange", 5);
        executeDraw = declared(renderer, "executeDraw", 2);
        Class<?> drawType = Class.forName(GuiRenderer.class.getName() + "$Draw");
        drawParts = drawType.getRecordComponents();
        for (RecordComponent part : drawParts) {
            part.getAccessor().setAccessible(true);
            if (part.getName().equals("textureSetup")) {
                drawTextures = part.getAccessor();
            }
        }
        drawConstructor = drawType.getDeclaredConstructors()[0];
        drawConstructor.setAccessible(true);

        Class<?> pipelineType = type("pipeline.RenderPipeline", "com.mojang.blaze3d.pipeline.RenderPipeline");
        Class<?> snippet = Class.forName(pipelineType.getName() + "$Snippet");
        Class<?> layoutType = type("pipeline.BindGroupLayout", "com.mojang.blaze3d.pipeline.BindGroupLayout");
        Object uniformBuffer = staticField(type("pipeline.UniformType", "com.mojang.blaze3d.shaders.UniformType"),
                "UNIFORM_BUFFER");
        Object layout = call(call(call(layoutType, "builder"), "withUniform", "GlassInfo", uniformBuffer), "build");
        Object blend = staticField(type("pipeline.BlendFunction", "com.mojang.blaze3d.pipeline.BlendFunction"),
                "TRANSLUCENT");
        Object target = type("pipeline.ColorTargetState", "com.mojang.blaze3d.pipeline.ColorTargetState")
                .getConstructor(blend.getClass()).newInstance(blend);
        Object quads = staticField(type("pipeline.PrimitiveTopology", "com.mojang.blaze3d.PrimitiveTopology"),
                "QUADS");
        Object format = staticField(DefaultVertexFormat.class, "POSITION_TEX_COLOR");

        Object builder = call(pipelineType, "builder", Array.newInstance(snippet, 0));
        builder = call(builder, "withLocation", ProteusClient.id("pipeline/glass"));
        for (String group : new String[] {"GLOBALS", "PROJECTION", "DYNAMIC_TRANSFORMS", "SAMPLER0"}) {
            builder = call(builder, "withBindGroupLayout", staticField(BindGroupLayouts.class, group));
        }
        builder = call(builder, "withBindGroupLayout", layout);
        builder = call(builder, "withVertexShader", ProteusClient.id("core/glass"));
        builder = call(builder, "withFragmentShader", ProteusClient.id("core/glass"));
        builder = call(builder, "withColorTargetState", target);
        builder = call(builder, "withVertexBinding", 0, format);
        builder = call(builder, "withPrimitiveTopology", quads);
        pipeline = call(builder, "build");

        boolean compiled = renderpearl
                ? call(RenderSystem.class, "getCompiledPipelineNullable", pipeline) != null
                : (Boolean) call(call(device(), "precompilePipeline", pipeline), "isValid");
        if (!compiled) {
            throw new IllegalStateException("the glass shader did not compile");
        }
        Object filter = staticField(type("textures.FilterMode", "com.mojang.blaze3d.textures.FilterMode"), "LINEAR");
        sampler = call(call(RenderSystem.class, "getSamplerCache"), "getClampToEdge", filter);
        ready = true;
    }

    private static void resize(RenderTarget main) throws ReflectiveOperationException {
        for (Object view : VIEWS) {
            call(view, "close");
        }
        VIEWS.clear();
        PARAMS.clear();
        SLOTS.clear();
        if (texture != null) {
            call(texture, "close");
        }
        Object format = call(call(main, "getColorTexture"), "getFormat");
        int usage = constant("textures.GpuTexture", "com.mojang.blaze3d.textures.GpuTexture", "USAGE_COPY_DST")
                | constant("textures.GpuTexture", "com.mojang.blaze3d.textures.GpuTexture", "USAGE_TEXTURE_BINDING");
        texture = call(device(), "createTexture", "Proteus glass backdrop", usage, format, main.width, main.height, 1,
                1);
        textureWidth = main.width;
        textureHeight = main.height;
    }

    private static void fail(Throwable problem) {
        if (!failed) {
            ProteusClient.LOGGER.warn("Proteus: glass could not be drawn, drawing it flat", problem);
        }
        failed = true;
    }

    private static Object device() throws ReflectiveOperationException {
        return call(RenderSystem.class, "getDevice");
    }

    private static boolean present(String name) {
        return GlassRenderer.class.getClassLoader().getResource(name.replace('.', '/') + ".class") != null;
    }

    private static Class<?> type(String modern, String legacy) throws ClassNotFoundException {
        return Class.forName(renderpearl ? RENDERPEARL + modern : legacy);
    }

    private static int constant(String modern, String legacy, String name) throws ReflectiveOperationException {
        return type(modern, legacy).getField(name).getInt(null);
    }

    private static Object staticField(Class<?> type, String name) throws ReflectiveOperationException {
        return type.getField(name).get(null);
    }

    private static Method declared(Class<?> owner, String name, int parameters) throws NoSuchMethodException {
        for (Method method : owner.getDeclaredMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == parameters) {
                method.setAccessible(true);
                return method;
            }
        }
        throw new NoSuchMethodException(owner.getName() + "." + name);
    }

    private static Object call(Object target, String name, Object... args) throws ReflectiveOperationException {
        boolean statics = target instanceof Class<?>;
        Class<?> owner = statics ? (Class<?>) target : target.getClass();
        Method method = find(owner, name, args, statics);
        try {
            return method.invoke(statics ? null : target, args);
        } catch (InvocationTargetException exception) {
            if (exception.getCause() instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw exception;
        }
    }

    private static Method find(Class<?> owner, String name, Object[] args, boolean statics)
            throws NoSuchMethodException {
        StringBuilder key = new StringBuilder(owner.getName()).append('.').append(name);
        for (Object arg : args) {
            key.append(',').append(arg == null ? "null" : arg.getClass().getName());
        }
        Method cached = METHODS.get(key.toString());
        if (cached != null) {
            return cached;
        }
        for (Method method : owner.getMethods()) {
            if (!method.getName().equals(name) || method.getParameterCount() != args.length
                    || Modifier.isStatic(method.getModifiers()) != statics) {
                continue;
            }
            Class<?>[] types = method.getParameterTypes();
            boolean fits = true;
            for (int i = 0; i < types.length && fits; i++) {
                fits = fits(types[i], args[i]);
            }
            if (fits) {
                method.setAccessible(true);
                METHODS.put(key.toString(), method);
                return method;
            }
        }
        throw new NoSuchMethodException(owner.getName() + "." + name + " with " + args.length + " arguments");
    }

    private static boolean fits(Class<?> type, Object arg) {
        if (arg == null) {
            return !type.isPrimitive();
        }
        if (!type.isPrimitive()) {
            return type.isInstance(arg);
        }
        return type == int.class && arg instanceof Integer
                || type == long.class && arg instanceof Long
                || type == float.class && arg instanceof Float
                || type == double.class && arg instanceof Double
                || type == boolean.class && arg instanceof Boolean;
    }
}
