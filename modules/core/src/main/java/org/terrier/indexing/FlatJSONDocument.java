package org.terrier.indexing;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.terrier.indexing.tokenisation.Tokeniser;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.ArrayUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

/**
 * This is a Terrier Document implementation of a document stored in JSON format. It assumes that
 * a single JSON document has at least a single attribute called 'text' that contains the text of
 * the document. 
 * 
 * Fields:
 * This implementation supports a single field named 'TEXT' by default. FieldTags.process is a
 * comma delimited list of properties to use as fields.
 * 
 * Meta-Data:
 * During the parsing process, the properties of each FlatJSONDocument is decorated with document meta-data.
 * This decoration process is performed by 'flattening' the layered structure of the JSON object and its
 * sub-attributes into individual properties. For property naming, attributes in different layers are connected
 * with a dot '.', e.g. user.name
 * 
 * @author Richard McCreadie and Saul Vargas
 * @since 5.1
 */
public class FlatJSONDocument implements Document {

	// constructor properties
	protected Map<String, String> properties;
	protected Tokeniser tokenizer;
    public String[][] tokens;
    protected List<String> fieldQueue;
    Map<String,Set<String>> fieldSet;
    
    // static properties
    protected String[] fieldsToProcess = ArrayUtils.parseCommaDelimitedString(ApplicationSetup.getProperty("FieldTags.process", "text"));
    
    // state
    protected int fieldIndex = 0;
    protected int tokenIndex = -1;
    protected int remainingTokens;
    
    
    public FlatJSONDocument(JsonObject json) {
		initalize(json.toString());
	}
    
    public FlatJSONDocument(String rawJson) throws JsonParseException, JsonMappingException, IOException {
    	initalize(rawJson);
    }
    
    @SuppressWarnings("unchecked")
    protected void initalize(String rawJson) {
    	try {
    		
			
			this.tokenizer = Tokeniser.getTokeniser();
			ObjectMapper mapper = new ObjectMapper();
			Map<Object, Object> nestedMap = mapper.readValue(rawJson, Map.class);
			properties = flatten("", nestedMap);
			
			if (properties.containsKey("id")) properties.put("docno", properties.get("id"));
			properties.put("srcjson", rawJson);
			
			fieldQueue = new ArrayList<String>(fieldsToProcess.length);
			tokens = new String[fieldsToProcess.length][];
			fieldSet = new HashMap<String,Set<String>>();
			
			int i =0;
			remainingTokens = 0;
			for (String fieldName : fieldsToProcess) {
				fieldName = fieldName.toLowerCase();
				fieldQueue.add(fieldName);
				if (properties.containsKey(fieldName)) {
					tokens[i] = tokenizer.getTokens(properties.get(fieldName));
					remainingTokens+=tokens[i].length;
					Set<String> fieldNameSet = new HashSet<String>();
					fieldNameSet.add(fieldName);
					fieldSet.put(fieldName, fieldNameSet);
				} else {
					tokens[i] = new String[0];
				}
				i++;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    
    @SuppressWarnings("unchecked")
    private static Map<String, String> flatten(String sk, Map<Object, Object> sv) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> map = new HashMap<>();
        
        String suffix = sk.isEmpty() ? "" : sk + ".";
        
        sv.forEach((k, v) -> {
            if (v instanceof String) {
                map.put(suffix + k, v.toString());
            } else if (v instanceof Map) {
                map.putAll(flatten(suffix + k, (Map<Object, Object>) v));
            } else {
                try {
                    map.put(suffix + k, mapper.writeValueAsString(v));
                } catch (Exception ex) {
                    Logger.getLogger(FlatJSONDocument.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        return map;
    }
    
    
    @Override
    public boolean endOfDocument() {
    	return remainingTokens>0;
    	
    }

    @Override
    public Map<String, String> getAllProperties() {
        return properties;
    }

    @Override
    public Set<String> getFields() {
    	if (fieldIndex<fieldsToProcess.length) return fieldSet.get(fieldQueue.get(fieldIndex));
    	else return null;
    }

    @Override
    public String getNextTerm() {
    	if (fieldIndex<fieldsToProcess.length) {
    		if (tokenIndex<tokens[fieldIndex].length-1) {
    			tokenIndex++;
    			remainingTokens--;
    			return tokens[fieldIndex][tokenIndex].trim();
    		} else {
    			fieldIndex++;
    			tokenIndex = -1;
    			return getNextTerm();
    		}
    	} else return null;
    }

    @Override
    public String getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public Reader getReader() {
    	StringBuilder builder = new StringBuilder();
    	for (String fieldName : fieldsToProcess) {
    		fieldName = fieldName.toLowerCase();
			if (properties.containsKey(fieldName)) {
				builder.append(properties.get(fieldName));
				builder.append(" ");
			}
    	}
    	return new StringReader(builder.toString().trim());
    }
    
   
}
