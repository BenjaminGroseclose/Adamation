package com.ianmann.mind;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
<<<<<<< Updated upstream
import java.util.Arrays;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.ianmann.mind.core.Constants;
import com.ianmann.mind.core.navigation.Category;
=======
import com.ianmann.database.fields.CharField;
import com.ianmann.database.fields.ForeignKey;
import com.ianmann.database.fields.IntegerField;
import com.ianmann.database.fields.ManyToManyField;
import com.ianmann.database.orm.Model;
import com.ianmann.database.orm.QuerySet;
import com.ianmann.database.orm.queries.scripts.WhereCondition;
import com.ianmann.database.utils.exceptions.ObjectAlreadyExistsException;
import com.ianmann.database.utils.exceptions.ObjectNotFoundException;
>>>>>>> Stashed changes
import com.ianmann.mind.emotions.EmotionUnit;
import com.ianmann.mind.utils.Serializer;
import com.ianmann.utils.utilities.Files;
import com.ianmann.utils.utilities.JSONUtils;

/**
 * Root class for all thoughts. Every thought object
 * will inherit {@code Neuron}.
 * @author kirkp1ia
 *
 */
public class Neuron implements Serializable {

	/**
	 * Category that is the parent of this one.
	 * If null, this is a root category.
	 */
	protected File parentCategory;
	
	/**
	 * References to any neuron that is linked to this neuron.
	 * The AI will use this list to link through the thoughts.
	 * The file it points to contains one {@code NeuralPathway} object
	 * and can be thought of as a synaptic connection.
	 */
	protected ArrayList<File> SynapticEndings;
	
	/**
	 * File in which this object is stored.
	 */
	public File location;
	
	/**
	 * EmotionUnit that is associated with this thought
	 */
	protected EmotionUnit associatedEmotion;
	
	/**
	 * Used by developers or other users looking into the AI
	 * to get a sense of what this neuron actually stands for.
	 * <br><br>
	 * The file containing this neuron will be called this label if
	 * it is not null.
	 */
	protected String associatedMorpheme;
	
	/**
<<<<<<< Updated upstream
	 * Used for parsing json into a neuron object.
	 */
	protected Neuron() {
		
	}
=======
	 * Denotes what type of neuron this is. The possible values are in the class NeuronType.
	 */
	public IntegerField type = new IntegerField("type", false, NeuronType.DESCRIPTION, false, 100);

	/**
	 * The neurons that represent synaptic connections to this neuron's parent neuron.
	 */
	public ManyToManyField<Neuron, NeuralPathway> parent_neurons = new ManyToManyField<Neuron, NeuralPathway>("parent_neurons", Neuron.class, NeuralPathway.class);
	
	/**
	 * The neurons that represent synaptic connections to this neuron.
	 */
	public ManyToManyField<Neuron, NeuralPathway> synaptic_connections = new ManyToManyField<Neuron, NeuralPathway>("synaptic_connections", Neuron.class, NeuralPathway.class);
>>>>>>> Stashed changes
	
	/**
	 * Create Neuron with an existing neuron linked to it.
	 * @param _linkedThought
	 * @param _associated
	 */
	public Neuron(Neuron _linkedThought, EmotionUnit _associated, Category _category) {
		this.initialize(_linkedThought, _associated, null, _category);
	}
	
	/**
	 * Create Neuron with an existing neuron linked to it.
	 * This takes a string that can later be used by a developer
	 * to have a sense of what this neuron represents.
	 * @param _linkedThought
	 * @param _associated
	 */
	public Neuron(Neuron _linkedThought, EmotionUnit _associated, String _label, Category _category) {
		this.associatedMorpheme = _label;
		this.initialize(_linkedThought, _associated, _label, _category);
	}
	
	/**
	 * Constructors should call this method to do all the final attribute initialization.
	 * @param _linkedThought
	 * @param _associated
	 * @param _label
	 */
	private void initialize(Neuron _linkedThought, EmotionUnit _associated, String _label, Category _category) {
		this.setParentCategory(_category);
		this.SynapticEndings = new ArrayList<File>();
		
		this.setAssociatedMorpheme(_label);
		this.associatedEmotion = _associated;
		this.location = new File(this.getFileLocation());
		this.addNeuralPathway(_linkedThought);
	}
	
	/**
	 * Set the morpheme that is associated with this neuron.
	 * @param _morpheme
	 */
	public void setAssociatedMorpheme(String _morpheme) {
		this.associatedMorpheme = _morpheme;
	}
	
	/**
	 * Return the morpheme associated with this neuron.
	 * @return
	 */
	public String getAssociatedMorpheme() {
		return this.associatedMorpheme;
	}
	
	/**
	 * Return the parent Category of this neuron from it's file.
	 * @return
	 */
	public Category getParentCategory() {
		try {
			return Category.parse(this.parentCategory);
		} catch (FileNotFoundException | ParseException e) {
			return null;
		}
	}
	
	/**
	 * Set the parent Category of this neuron.
	 * @param _category
	 */
	public void setParentCategory(Category _category) {
		if (_category == null) {
			return;
		} else {
			this.parentCategory = _category.location;
			Neuron c = this.getParentCategory();
			c.addNeuralPathway(this);
			c.save();
		}
	}
	
	/**
	 * Moves this neuron to the category represented
	 * by _category. This method also saves _category
	 * if it's not null or the same category as it use
	 * to be assimilated with. It also saves this neuron.
	 * @param _category
	 */
	public void assimilate(Category _category) {
		// If this neuron has a parent category already and it's not the same as _category
		if (this.parentCategory != null && !this.getParentCategory().equals(_category)) {
			// Remove this file from this.parentCategory's folder.
			this.location.delete();
			this.location = null; // Set it null so that getFileLocation() will do logic to get the new path.
			Neuron c = this.getParentCategory();
			c.removeNeuralPathway(this);
			c.save();
		}
		this.setParentCategory(_category);
		// Store this neuron in _category
		this.location = new File(this.getFileLocation());
		this.save();
	}
	
	/**
	 * Retrieve the location to the file containing this Neuron.
	 * <br><br>
	 * This method does the logic for deciding what to name the file.
	 * If no associated morpheme is found for this neuron, it will
	 * use an id from neuron ids file.
	 */
	protected String getFileLocation() {
		if (this.location == null) {
			// Get category folder
			String pathToCategory = "";
			if (this.parentCategory != null) {
				pathToCategory = this.getParentCategory().getCategoryPath();
			} else {
				// no category so just set pathToCategory to the root neuron folder
				pathToCategory = Constants.NEURON_ROOT;
			}
			
			if (this.associatedMorpheme == null) {
				/*
				 * If no morpheme is found for this neuron,
				 * grab the id out of id file and use that for
				 * the file name. Then increment the next id.
				 */
				Scanner s;
				try {
					s = new Scanner(new File(Constants.NEURON_ROOT + "ids"));
					int next = s.nextInt();
					s.close();
					PrintWriter p = new PrintWriter(new File(Constants.NEURON_ROOT + "ids"));
					p.print(next+1);
					p.close();
					return pathToCategory + String.valueOf(next) + ".nrn";
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			} else {
				/*
				 * A morpheme is saved for this neuron so just use
				 * that as the file name.
				 */
				return pathToCategory + this.associatedMorpheme + ".nrn";
			}
		} else {
			/*
			 * The location is already stored so just return it.
			 */
			return this.location.getPath();
		}
	}
	
	/**
	 * Return the index at which _neuron is stored in
	 * {@code this.SynapticEndings}.
	 * @param _neuron
	 * @return
	 */
	private int indexOfNeuralPathway(Neuron _neuron) {
		for (int i = 0; i < this.SynapticEndings.size(); i++) {
			if (this.SynapticEndings.get(i).equals(_neuron)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Make new pathway to a thought.
	 * @param _thought
	 */
	public void addNeuralPathway(Neuron _thought) {
		if (_thought != null) {
			NeuralPathway t = new NeuralPathway(_thought.location);
			this.SynapticEndings.add(t.location);
			this.save();
		}
	}
	
	/**
	 * Remove the pathway to a thought.
	 * @param _thought
	 */
	public void removeNeuralPathway(Neuron _thought) {
		if (_thought != null) {
			int indexOfPathway = this.indexOfNeuralPathway(_thought);
			if (indexOfPathway != -1) {
				this.SynapticEndings.remove(indexOfPathway);
				this.save();
			} else {
				return;
			}
		}
	}
	
	/**
	 * Return a list of neurons that are associated with this neuron.
	 * The neurons returned will be stored in the folder or subfolder
	 * of the folder represented by the given path in _category.
	 * 
	 * @param _category - Neuron with path to folder that contains the
	 * Neurons desired. returns neurons in subfolders of _category as well.
	 */
	public ArrayList<Neuron> getRelatedNeuronsInCategory(Category _category) {
		String categoryFolder = _category.getCategoryPath();
		ArrayList<Neuron> neurons = new ArrayList<Neuron>();
		
		for (File neuronFile : this.SynapticEndings) {
			String pathFromNeuronRoot = neuronFile.getAbsolutePath().split(Constants.NEURON_ROOT)[1];
			if (pathFromNeuronRoot.startsWith(categoryFolder)) {
				try {
					neurons.add(Neuron.parse(neuronFile));
				} catch (FileNotFoundException | ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return neurons;
	}
	
	/**
	 * Remove the neuron file that stores this neuron.
	 */
	protected void destroy() {
		this.location.delete();
		this.location = null;
	}
	
	/**
	 * Print this object to the file at {@link Neuron.location}.
	 * <br><br>
	 * If the neuron file already exists, just rewrite the data
	 * in the file, overwriting the old data with the new data.
	 */
	public void save() {
		FileOutputStream fos = null;
		try {
			try {
				if (!this.location.exists()) {
					java.nio.file.Files.createFile(this.location.toPath());
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			PrintWriter objWriter = new PrintWriter(this.location);
			objWriter.print(JSONUtils.formatJSON(this.jsonify(), 0));
			objWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean equals(Neuron o) {
		return this.location.equals(o.location);
	}
	
	/**
	 * Parse json data in a file into a Neuron object
	 * @param _neuronFile
	 * @return
	 * @throws FileNotFoundException
	 * @throws ParseException
	 */
	public static Neuron parse(File _neuronFile) throws FileNotFoundException, ParseException {
		JSONObject jsonNeuron = (JSONObject) Files.json(_neuronFile);
		
		Neuron n = new Neuron();
		
		String parentCategoryString = (String) jsonNeuron.get("parentCategory");
		if (!parentCategoryString.equals("NO_CATEGORY")) {
			n.parentCategory = new File(Constants.STORAGE_ROOT + (String) jsonNeuron.get("parentCategory"));
		} else {
			n.parentCategory = null;
		}
		
		n.SynapticEndings = new ArrayList<File>();
		JSONArray synaptics = (JSONArray) jsonNeuron.get("synapticEndings");
		for (Object path : synaptics) {
			String filePath = Constants.STORAGE_ROOT + (String) path;
			n.SynapticEndings.add(new File(filePath));
		}
		
		n.location = new File(Constants.STORAGE_ROOT + (String) jsonNeuron.get("location"));
		
		n.associatedEmotion = EmotionUnit.getEmotion((String) jsonNeuron.get("associatedEmotion"));
		
		if (!(jsonNeuron.get("associatedMorpheme") instanceof Long)) {
			n.associatedMorpheme = (String) jsonNeuron.get("associatedMorpheme");
		} else {
			n.associatedEmotion = null;
		}
		
		return n;
	}
	
	/**
	 * Return the neuron object as a json object.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public JSONObject jsonify() {
		JSONObject neuronJson = new JSONObject();
		
		if (this.parentCategory != null) {
			neuronJson.put("parentCategory", this.parentCategory.getAbsolutePath().split(Constants.STORAGE_ROOT)[1]);
		} else {
			neuronJson.put("parentCategory", "NO_CATEGORY");
		}
		
		neuronJson.put("synapticEndings", new JSONArray());
		for (File synapse : this.SynapticEndings) {
			((JSONArray) neuronJson.get("synapticEndings")).add(synapse.getAbsolutePath().split(Constants.STORAGE_ROOT)[1]);
		}
		neuronJson.put("location", this.location.getAbsolutePath().split(Constants.STORAGE_ROOT)[1]);
		neuronJson.put("associatedEmotion", this.associatedEmotion.getName());
		if (this.associatedMorpheme != null) {
			neuronJson.put("associatedMorpheme", this.associatedMorpheme);
		} else {
			neuronJson.put("associatedMorpheme", 1);
		}
		
		return neuronJson;
	}
}