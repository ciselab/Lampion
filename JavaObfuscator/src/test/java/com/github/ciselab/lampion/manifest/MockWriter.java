package com.github.ciselab.lampion.manifest;

import com.github.ciselab.lampion.transformations.TransformationResult;

import java.util.ArrayList;
import java.util.List;

public class MockWriter implements ManifestWriter{

    public List<TransformationResult> receivedResults = new ArrayList<>();

    public boolean wasTouched = false;

    @Override
    public void writeManifest(List<TransformationResult> results) {
        wasTouched = true;
        this.receivedResults = results;
    }
}
