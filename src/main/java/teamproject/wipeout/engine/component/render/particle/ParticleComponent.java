package teamproject.wipeout.engine.component.render.particle;

import teamproject.wipeout.engine.component.GameComponent;

public class ParticleComponent implements GameComponent {

    public ParticleParameters parameters;
    public double time;
    public double lastEmissionTime;
    public int nextBurst;

    private boolean playing;
    
    public ParticleComponent(ParticleParameters parameters) {
        this.parameters = parameters;
        this.playing = false;
    }

    public void play() {
        this.playing = true;
        this.time = 0;
        this.lastEmissionTime = this.time;
        this.nextBurst = 0;
    }

    public void stop() {
        this.playing = false;
    }

    public boolean isPlaying() {
        return this.playing;
    }

    public String getType() {
        return "particle";
    }
}
