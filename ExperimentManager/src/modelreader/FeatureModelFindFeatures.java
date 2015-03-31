package modelreader;

import backend.*;
import model.*;
import java.util.*;


public class FeatureModelFindFeatures {
	
	private Feature root;
	private int parentSize;
	private ArrayList<Feature> visit = new ArrayList<Feature>();
	ArrayList<Feature> tempParent = new ArrayList<Feature>();
	private int featureSize =0;
	
	/**
	 * Takes the name of the model and find the root
	 * then loops down the feature model child by child to find the parents
	 * 
	 * @param model - String
	 */
	public void findParents(String model){
		Feature root;
		Feature parent;
		boolean loop = true;
		
		FeatureModelLoader loader = new FeatureModelLoader(model);
		FeatureModel featureModel = loader.get_feature_model();
		root = featureModel.getRoot();
		setRoot(root);
		ArrayList<Feature> allFeatures = ListFeatures(root);
		parent = root;
		while(loop){
			List<Feature> childrenFeatures = parent.getChildren();
			featureSize += childrenFeatures.size();
				
			int i = 0 ;
			while(i < childrenFeatures.size()){
				if(!(visit.contains(childrenFeatures.get(i)))){   //if hasn't been visited find path.
					parent = childrenFeatures.get(i);
					parent = find(parent);
					break;
				}
				if(i == childrenFeatures.size()){
					parent = parent.getParent();
					i = 0;
				}
				i++;
			}
			
			if(!(tempParent.contains(parent))&& !(visit.contains(parent))){
				tempParent.add(parent);
				visit.add(parent);
				visit.add(parent.getParent());
				parentSize++;
			}
				
			if(!(parent.isRoot())){
				parent = parent.getParent();
			}
			else if(parent.isRoot()){
				loop = false;
			}
		}
		
		toArray();
	}
	
	
	/**
	 * FIND - loops down the feature-model until parent feature doesn't have children
	 * then it takes parent of that leaf.
	 * @param parent - Feature
	 * @return 
	 */
	public Feature find(Feature parent) {
		while(parent.hasChildren()){
			List<Feature> childrenFeatures = parent.getChildren();
			parent = childrenFeatures.get(0);
		}
		return parent.getParent();
	}
	public void setRoot(Feature rootin){
		root = rootin;
	}
	
	/**
	 * returns the root of current Feature Model
	 * @return root - Feature
	 */
	public Feature gotRoot(){
		return root;
	}
	
	/**
	 * return size of parentArray
	 * @return parentSize - int
	 */
	public int getSize(){
		return parentSize;
	}
	
	/**	
	 * toArray takes arrayList of features and returns them in an array.
	 * @return ar - Feature[]
	 */
	public Feature[] toArray(){
		Feature[] ar = new Feature[parentSize];
		for(int i = 0 ; i < parentSize; i++){
			ar[i] = tempParent.get(i);
		}
		return ar;
	}
	
	public int featureSize(){
		return featureSize;
	}
	public ArrayList<Feature> ListFeatures(Feature root){
		ArrayList<Feature> arl = new ArrayList<Feature>();
		arl.add(root);
		int head = -1;
		int tail = 0;
		
		
		Feature next;
		while(head < tail){
			head++;
			next = arl.get(head);
			if(next.hasChildren()){
				List<Feature> list = next.getChildren();
				for(int i = 0 ; i < list.size() ; i++){
					tail++;
					arl.add(list.get(i));
					
				}
			}	
		}

		
		return arl;
	}
	
	
}