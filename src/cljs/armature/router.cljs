(ns armature.router)

(defn router
  "Extendable router that takes a hashmap of URIs to handler functions
   Should be able to split up routes and merge them into a single hashmap

   IDEAS:
   - Local scope router that only calls handlers within certain views
     This way you could implement a back button for every action easily

   - Implement with core.async to create an event loop with triggers and handlers
   - Routers are just an event loop. Therefore bindings can be within a local scope
  "
  [routes]
  )
