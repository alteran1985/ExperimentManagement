package backend;
import configuration.*;
import model.*;
import java.util.*;

public class FeatureModelManager {
	private FeatureModel featureModel;
	public FeatureModelManager(){
		FeatureModelLoader modelLoader = new FeatureModelLoader();
		featureModel = modelLoader.get_feature_model();
	}
	
	public LinkedList<Feature> obtainFeatures(String phase, Configuration configuration){
		LinkedList<Feature> allFeatures;
		LinkedList<Feature> availableFeatures=new LinkedList<Feature>();
		Feature currentNode;
		
		currentNode = configuration.getSelectablefeature(phase).getFeature();
		allFeatures = currentNode.getChildren();
		Iterator<Feature> iter = allFeatures.iterator();
		while(iter.hasNext()){
			Feature nextFeature = iter.next();
			if (configuration.getSelectablefeature(nextFeature.getName()).getSelection()!=Selection.UNSELECTED){
				availableFeatures.add(nextFeature);
			}
		}
		
		return availableFeatures;
	}
	
	public Configuration updateConfiguration(Configuration configuration, LinkedList<Feature> changedFeatures, ArrayList<Boolean> newValues){
		int i=0;
		Iterator<Feature> iter = changedFeatures.iterator();
		while (iter.hasNext()){
			String featureName = iter.next().getName();
			boolean value = newValues.get(i);
			if(value){
				configuration.setManual(configuration.getSelectablefeature(featureName), Selection.SELECTED);
			}else{
				configuration.setManual(configuration.getSelectablefeature(featureName), Selection.UNSELECTED);
			}
		}
		return configuration;
	}
	
	public FeatureModel getFeatureModel(){
		return this.featureModel;
	}
}
