package ch.ge.cti.logchainer.beans;

import java.util.ArrayList;

public class Flux {
    private final String fluxname;
    private ArrayList<FileWatched> filesWatched;

    public Flux(String fluxname) {
	this.fluxname = fluxname;
	this.filesWatched = new ArrayList<FileWatched>();
    }
    
    public String getFluxname() {
        return fluxname;
    }
    
    public ArrayList<FileWatched> getFilesWatched() {
        return filesWatched;
    }
}
