package server.data.facade.interfaces;

import java.util.Set;

import server.firstleveldomainservices.secondleveldomainservices.subscriptionservice.Subscription;

public interface ISubscriptionFacade {
    public void saveSubscription(Subscription subscription);
    public Set<Subscription> getAllSubs();
    public void deleteSubscription(Subscription subscription);
}
