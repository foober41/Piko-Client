package piko.animation;

/** Easing curves used by the Piko interface. */
public enum Easing {

    LINEAR {
        @Override
        public float apply(float progress) {
            return progress;
        }
    },
    EASE_OUT_QUAD {
        @Override
        public float apply(float progress) {
            return 1.0F - (1.0F - progress) * (1.0F - progress);
        }
    },
    EASE_OUT_CUBIC {
        @Override
        public float apply(float progress) {
            float inverted = 1.0F - progress;
            return 1.0F - inverted * inverted * inverted;
        }
    },
    EASE_IN_OUT_QUAD {
        @Override
        public float apply(float progress) {
            return progress < 0.5F
                    ? 2.0F * progress * progress
                    : 1.0F - (float) Math.pow(-2.0F * progress + 2.0F, 2) / 2.0F;
        }
    },
    EASE_OUT_BACK {
        @Override
        public float apply(float progress) {
            float c1 = 1.70158F;
            float c3 = c1 + 1.0F;
            float inverted = progress - 1.0F;
            return 1.0F + c3 * inverted * inverted * inverted + c1 * inverted * inverted;
        }
    };

    public abstract float apply(float progress);
}
