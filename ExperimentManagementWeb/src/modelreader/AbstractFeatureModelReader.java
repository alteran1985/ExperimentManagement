/* FeatureIDE - A Framework for Feature-Oriented Software Development
 * Copyright (C) 2005-2013  FeatureIDE team, University of Magdeburg, Germany
 *
 * This file is part of FeatureIDE.
 * 
 * FeatureIDE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * FeatureIDE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with FeatureIDE.  If not, see <http://www.gnu.org/licenses/>.
 *
 * See http://www.fosd.de/featureide/ for further information.
 */
package modelreader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import model.FeatureModel;

/**
 * Default reader to be extended for each feature model format.
 * 
 * If IFile support is needed, the {@link FeatureModelReaderIFileWrapper} has to be used.
 * 
 * @author Thomas Thuem
 */
public abstract class AbstractFeatureModelReader {

	/**
	 * the structure to store the parsed data
	 */
	protected FeatureModel featureModel;
	
	public void setFeatureModel(FeatureModel featureModel) {
		this.featureModel = featureModel;
	}
	
	public FeatureModel getFeatureModel() {
		return featureModel;
	}
	
	public void readFromFile(File file) throws Exception, FileNotFoundException {
		String fileName = file.getPath();		
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(fileName);
		    parseInputStream(inputStream);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
 	}

	public void readFromString(String text) throws Exception {
        InputStream inputStream = new ByteArrayInputStream(text.getBytes(Charset.availableCharsets().get("UTF-8")));
        parseInputStream(inputStream);
 	}

	/**
	 * Reads a feature model from an input stream.
	 * 
	 * @param  inputStream  the textual representation of the feature model
	 * @throws UnsupportedModelException
	 */
	protected abstract void parseInputStream(InputStream inputStream)
			throws Exception;
	
}
