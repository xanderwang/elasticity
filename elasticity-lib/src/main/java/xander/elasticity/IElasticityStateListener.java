package xander.elasticity;

/**
 * A callback-listener enabling over-scroll effect clients to be notified of effect state transitions.
 * <br/>Invoked whenever state is transitioned onto one of {@link IElasticityState#STATE_IDLE},
 * {@link IElasticityState#STATE_DRAG_START_SIDE}, {@link IElasticityState#STATE_DRAG_END_SIDE}
 * or {@link IElasticityState#STATE_BOUNCE_BACK}.
 *
 * @author amit
 *
 * @see IElasticityUpdateListener
 */
public interface IElasticityStateListener {

    /**
     * The invoked callback.
     *
     * @param decor The associated over-scroll 'decorator'.
     * @param oldState The old over-scroll state; ID's specified by {@link IElasticityState}, e.g.
     *                 {@link IElasticityState#STATE_IDLE}.
     * @param newState The <b>new</b> over-scroll state; ID's specified by {@link IElasticityState},
     *                 e.g. {@link IElasticityState#STATE_IDLE}.
     */
    void onOverScrollStateChange(IElasticity decor, int oldState, int newState);
}
