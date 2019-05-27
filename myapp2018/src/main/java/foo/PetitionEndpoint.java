package foo;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query;


@Api(name = "myApi",
version = "v1",
namespace = @ApiNamespace(ownerDomain = "helloworld.example.com",
    ownerName = "helloworld.example.com",
    packagePath = ""))

public class PetitionEndpoint {
	
int i = 0;	

	
	// toute les petition
	@ApiMethod(name = "listAllPetition",path="listAllPetition", httpMethod=ApiMethod.HttpMethod.GET)
	public List<Entity> listAllPetition(){
		
			Query q =new Query("Petition");
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			PreparedQuery pq = datastore.prepare(q);		
			List<Entity> result = pq.asList(FetchOptions.Builder.withDefaults());
			return result;
			
		}
	
	
	// top 100 des petitions
	@ApiMethod(name="listTopPetition", path="listTopPetition", httpMethod=ApiMethod.HttpMethod.GET)
	public List<Entity> listTopPetition(){
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Query q = new Query("Petition").addSort("score", Query.SortDirection.DESCENDING);;
		PreparedQuery pq= ds.prepare(q);
		
		// recupére uniquement les 100 premiers resultats
		List<Entity> results=pq.asList(FetchOptions.Builder.withLimit(100));
		return results;
		}
	
	// recupère une petition 
	@ApiMethod(name="listPetition", path="listPetition", httpMethod=ApiMethod.HttpMethod.GET)
	public Entity listPetition(@Named("param") String param) {
		Query q = new Query("Petition").setFilter(new FilterPredicate("petition", FilterOperator.EQUAL, param));
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);		
		Entity result = pq.asSingleEntity();
		return result;
		
	}
	
	// poste une petition
	@ApiMethod(name="addPetition", path="addPetition", httpMethod=ApiMethod.HttpMethod.POST)
	public Entity addPetition(@Named("sender") String sender,  @Named("title") String title, @Named("body") String body) {
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		 
		Entity e = new Entity("Petition",body+title);
		e.setProperty("sender", sender);
		e.setProperty("title", title);
		e.setProperty("body", body);
		e.setProperty("score", 0);
		e.setProperty("signataires", null);
			
		datastore.put(e);
			return e;
		}
	
	
	// signer une petition
	@SuppressWarnings("unchecked")
	@ApiMethod(name="signer", path="signer", httpMethod=ApiMethod.HttpMethod.PUT)
    public Entity signer(@Named("name") String name, @Named("userId")String userId) throws EntityNotFoundException {
		if(userId != null)
		{
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			Key k1 = KeyFactory.createKey("Petition", name);
			Query q = new Query("Petition").setFilter(new FilterPredicate("__key__", FilterOperator.EQUAL, k1));
            PreparedQuery pq = datastore.prepare(q);		
    		Entity petition = pq.asSingleEntity();
            ArrayList<String> sign;
            
			if ((ArrayList)petition.getProperty("signataires") != null) { // on a des signataires
			        sign = (ArrayList<String>)petition.getProperty("signataires");
			        if (!sign.contains(userId)) {
				        sign.add(userId);
				        petition.setProperty("signataires", sign);
			            petition.setProperty("score", sign.size());
				        datastore.put(petition);
			        }
			}
			else { // si la lsite des signataires est null
					sign = new ArrayList<String>();
			        sign.add(userId);
			        petition.setProperty("signataires", sign);
		            petition.setProperty("score", sign.size());
			        datastore.put(petition);
			}
           return petition;
		}
		else {
			return null;
		}
			
    }	
	// Liste pétition POSTER par le USER
			@ApiMethod(name="listUserPetition", path="listUserPetition", httpMethod=ApiMethod.HttpMethod.GET)
			public List<Entity> listUserPetition(@Named("userId") String userId){

				DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
				Query q = new Query("Petition").setFilter(new FilterPredicate("sender", FilterOperator.EQUAL, userId));
				PreparedQuery pq= ds.prepare(q);


				List<Entity> results=pq.asList(FetchOptions.Builder.withDefaults());
				return results;
				}

	// Liste petition SIGNER par le USER
			@ApiMethod(name="listUserSignPetition", path="listUserSignPetition", httpMethod=ApiMethod.HttpMethod.GET)
			public List<Entity> listUserSignPetition(@Named("userId") String userId){
            	 DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
 				Query q = new Query("Petition");
 				PreparedQuery pq= ds.prepare(q);
 				List<Entity> results=pq.asList(FetchOptions.Builder.withDefaults());

 				for (int i=0;i<results.size();i++) {
 					ArrayList<String> sign = (ArrayList<String>) results.get(i).getProperty("signataires");
 					if (!sign.contains(userId)) results.remove(i);
 				}

 				return results;
 				} 
			
		
}
