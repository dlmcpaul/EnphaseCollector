package com.hz.models.envoy.json;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by David on 23-Oct-17.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AcbType extends TypeBase {
	private String state;
	private int percentFull;
}
