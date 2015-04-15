package test;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		FeatureModelLoader loader = new FeatureModelLoader("model.xml");
		String output = loader.check_feature_model();
		System.out.print(output);
	}

}
