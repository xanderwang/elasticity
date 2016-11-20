package xander.elasticity;

/**
 * A callback-listener enabling over-scroll effect clients to subscribe to <b>real-time</b> updates
 * of over-scrolling intensity, provided as the view-translation offset from pre-scroll position.
 *
 * @author amit
 *
 * @see IElasticityStateListener
 */
public interface IElasticityUpdateListener {

    /**
     * The invoked callback.
     *
     * @param decor The associated over-scroll 'decorator'.
     * @param state One of: {@link IElasticityState#STATE_IDLE}, {@link IElasticityState#STATE_DRAG_START_SIDE},
     *              {@link IElasticityState#STATE_DRAG_START_SIDE} or {@link IElasticityState#STATE_BOUNCE_BACK}.
     * @param offset The currently visible offset created due to over-scroll.
     */
    void onOverScrollUpdate(IElasticity decor, int state, float offset);
}
