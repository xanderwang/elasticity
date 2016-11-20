package xander.elasticity;

/**
 * @author amit
 */
public interface ElasticityListenerStubs {

    class ElasticityStateListenerStub implements IElasticityStateListener {
        @Override
        public void onOverScrollStateChange(IElasticity decor, int oldState, int newState) { }
    }

    class ElasticityUpdateListenerStub implements IElasticityUpdateListener {
        @Override
        public void onOverScrollUpdate(IElasticity decor, int state, float offset) { }
    }
}
