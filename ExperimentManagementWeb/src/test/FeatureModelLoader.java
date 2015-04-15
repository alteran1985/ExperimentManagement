package test;

import model.*;
import modelreader.*;

import java.io.*;
import java.util.*;

public class FeatureModelLoader {
	private FeatureModel featureModel;
	// Class constructor
	public FeatureModelLoader(String fileName){
		// Load the feature model
		XmlFeatureModelReader reader;
		featureModel = new FeatureModel();
		File modelFile = new File("/Test/model.xml");
		reader = new XmlFeatureModelReader(featureModel);
		try {
			reader.readFromFile(modelFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.print("File not found");
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.print("Model not supported");
			e.printStackTrace();
			System.out.print(e.getMessage());
		}
		/*
		if (featureModel instanceof ExtendedFeatureModel)
			reader = ModelIOFactory.getModelReader(featureModel, ModelIOFactory.TYPE_VELVET);
		else
			reader = ModelIOFactory.getModelReader(featureModel, ModelIOFactory.TYPE_XML);
		try {
			reader.readFromFile(modelFile);
		} catch (FileNotFoundException e) {
			FMUIPlugin.getDefault().logError(e);
		} catch (UnsupportedModelException e) {
			FMUIPlugin.getDefault().logError(e);
		}*/
		// Load the configuration
		featureModel = reader.getFeatureModel();
		// Modify the configuration

	}
	
	public String check_feature_model(){
		Set<String> feature_names = featureModel.getFeatureNames();
		return feature_names.toString();
	}
}
