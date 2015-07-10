package eu.crushedpixel.replaymod.holders;

import eu.crushedpixel.replaymod.interpolation.Interpolate;
import eu.crushedpixel.replaymod.interpolation.KeyframeValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Position implements KeyframeValue {

    @Interpolate
    public double x, y, z;

    @Override
    public Position newInstance() {
        return new Position();
    }

}
