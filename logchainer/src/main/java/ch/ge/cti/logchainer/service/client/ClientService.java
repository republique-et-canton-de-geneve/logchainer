package ch.ge.cti.logchainer.service.client;

import java.util.ArrayList;

import ch.ge.cti.logchainer.beans.Client;

public interface ClientService {
    void registerEvent(Client client);
    
    void deleteAllTreatedFluxFromMap(ArrayList<String> allDoneFlux, Client client);
}
