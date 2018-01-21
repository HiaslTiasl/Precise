==============================================
 JavaScript Libraries
==============================================

This is a short description of the libraries used in this project.

(Order is lexicographically by filename)

angular.js:
	Website:
		https://angularjs.org/
	Source code:
		https://github.com/angular/angular.js
	Dependencies:
		None (jQuery is optional)
	Description:
		A general JavaScript framework for web applications.
		Provides a template engine with two-way data binding.
		Allows to extend HTML with custom components and directives.
		Provides dependency injection for components and services,
		thereby guiding the developer in structuring the code.
	Why/how it is used:
		Used as a general framework for structuring the code,
		because of the advantages listed in the description,
		and because of good previous experience.
	
angularjs-color-picker.js
	Website:
		https://ruhley.github.io/angular-color-picker/
	Source code:
		https://github.com/ruhley/angular-color-picker
	Dependencies:
		angular.js
	Description:
		A color-picker directive for angular.
	Why/how it is used:
		To let the user view and change colors of phases.
	
angular-toastr.tpls.js
	Website:
		http://foxandxss.github.io/angular-toastr/ (Demo)
	Source code:
		https://github.com/Foxandxss/angular-toastr
	Dependencies:
		angular.js
	Description:
		Angular service for showing toast messages, which are
		non-blocking.
		N.B. The extension .tpls.js indicates that this version includes
		the angular templates for displaying the messages, as opposed to
		an alternative version that requires the developer to provide
		custom templates.
	Why/how it is used:
		Error handling. Regardless of where the error happens,
		it can be shown in a nice way by just calling this service,
		without the need of reserving places on the page where
		errors are to be displayed, or disturbing the user with
		blocking dialogs.
		
	
angular-ui-router.js
	Website:
		https://ui-router.github.io/ng1/
	Source code:
		https://github.com/angular-ui/ui-router
	Dependencies:
		angular.js
	Description:
		A client-side single page application routing framework for Angular.
		Lets the developer define application states, which can be associated
		to the view to be shown and to a corresponding browser URL.
		Navigating through the app changes the browser URL, and vice-versa.
		This enables bookmarking URLs and using the browser's back and forth
		buttons while avoiding the need to reload the whole page on every step.
	Why/how it is used:
		To structure the app into an 'allModels' state and a 'singleModel' state,
		and the latter further into 'singleModel.config' and 'singleModel.diagram',
		all with different URLs.
		The URL for the 'singleModel' state includes the name of the model to be
		displayed, so it can be bookmarked.
		
backbone.js
	Website:
		http://backbonejs.org/
	Source code:
		https://github.com/jashkenas/backbone
	Dependencies:
		lodash.js (as mimic of underscore), for DOM manipulation jquery.js
	Description:
		A library (not a framework) providing minimum building blocks for an
		application structure: base classes for models, collections, and views,
		as well as an event system.
	Why/how it is used:
		Because JointJS depends on it, and in particular on version 1.1.3.
		It would be useful by itself, too, but we already use AngularJS, which
		(being a framework) offers more features.
	
console.js
	Website:
		http://ionicabizau.github.io/console.js/ (Demo)
	Source code:
		https://github.com/IonicaBizau/console.js
	Dependencies:
		none
	Description:
		"A JavaScript library that overrides the console object bringing its
		functionality in a DOM element."
	Why/how it is used:
		Only used during development for debugging on mobile devices, as
		they do not allow to see the logs to the console. 
	
hammer.js
	Website:
		http://hammerjs.github.io/
	Source code:
		https://github.com/hammerjs/hammer.js
	Dependencies:
		none
	Description:
		Detects common touch gestures such as pinch and swipe and allows
		to define custom gestures.
	Why/how it is used:
		For detecting pan and pinch gestures on touch devices for moving
		and zooming the paper and to map double-taps to double-clicks. 
	
joint.js
	Website:
		http://www.jointjs.com/opensource
	Source code:
		https://github.com/clientIO/joint
	Dependencies:
		jquery.js (v2.2.4), lodash.js (v3.10.1), backbone.js (1.1.3)
	Description:
		Library for creating interactive graph-based diagrams.
		Provides BackboneJS model and view classes for graphs, nodes,
		and links, which can be instantiated directly or extended to
		define rich custom diagrams.
		The diagram is rendered using SVG.
	Why/how it is used:
		To show the graphical representation of a diagram.
		This library was chosen because of the rich built-in functionality,
		such as controlling the shape of arrows by interactively defining
		control vertices.
		Note the recommended versions of the dependencies, which are *not*
		the most recent ones!
		
jquery.js
	Website:
		https://jquery.com/
	Source code:
		https://github.com/jquery/jquery
	Dependencies:
		none
	Description:
		General purpose JavaScript library that provides an easier API than the
		native browser one and ensures cross-browser compatibility. Supports
		DOM traversal and manipulation, event handling, AJAX etc.
	Why/how it is used:
		Because JointJS (and BackboneJS) depends on it, and in particular on
		version 2.2.4.
		Would be useful by itself, but we already use AngularJS, which supports
		the same use cases, but on a higher level of abstraction. Angular optionally
		uses jQuery, and provides a simple implementation of the most commonly used
		parts if jQuery is not available.
	
json-formatter.js
	Website:
		http://azimi.me/json-formatter/demo/demo.html
	Source code:
		https://github.com/mohsen1/json-formatter
	Dependencies:
		angular.js
	Description:
		Displays a JSON object with collapsible properties and syntax highlighting.
	Why/how it is used:
		To show the building hierarchy as defined in the JSON.
		Could be removed in favor of a better visualization in future versions.
	
lodash.js
	Website:
		https://lodash.com/
	Source code:
		https://github.com/lodash/lodash/
	Dependencies:
		none
	Description:
		General JavaScript utility library supporting processing of collections
		and general functional programming operations such as creating composite
		functions.
	Why/how it is used:
		On the one hand, because JointJS (and BackboneJS) depends on it, and in
		particular on version 3.10.1.
		On the other hand, it is used throughout the application because it helps
		in many situations and we have to load it anyway.
	
ng-file-upload.js
	Website:
		https://angular-file-upload.appspot.com/ (Demo)
	Source code:
		https://github.com/danialfarid/ng-file-upload
	Dependencies:
		angular.js
	Description:
		Angular directives and and services for file input and upload.
	Why/how it is used:
		For importing MDL files. More specifically, for listening to the event
		when a file was selected for uploading, for which Angular has no built-in
		support. The rest is handled using the native FileReader API and the standard
		Angular service for HTTP communication.
		May be removed in future versions in favor of a more lightweight solution.
	
require.js
	Website:
		http://requirejs.org/
	Source code:
		https://github.com/requirejs/requirejs
	Dependencies:
		none
	Description:
		A "JavaScript file and module loader".
		Implements the Asynchronous Module Definition (AMD) API for defining JavaScript
		modules that hide implementation details and declare dependencies to other modules
		so they can be loaded asynchronously.
	Why/how it is used:
		Primarily for maintainability, since we do not have to keep track of all the
		scripts and their dependencies in the index.html file.
	
smart-table.js
	Website:
		https://lorenzofox3.github.io/smart-table-website/
	Source code:
		https://github.com/lorenzofox3/Smart-Table
	Dependencies:
		angular.js
	Description:
		Flexible angular module providing enhanced table features such as filtering and sorting.
	Why/how it is used:
		For implementing filtering and sorting in the task definition table.
	
svg-pan-zoom.js
	Website:
		https://github.com/ariutta/svg-pan-zoom#demos (Demos)
	Source code:
		https://github.com/ariutta/svg-pan-zoom
	Dependencies:
		none
	Description:
		"JavaScript library that enables panning and zooming of an SVG in an HTML document,
		with mouse events or custom JavaScript hooks".
	Why/how it is used:
		To let the user move and zoom the paper. Mouse events are handled by default
		settings, while touch gestures are implemented by using hammer.js in the custom
		hooks.
	
tinycolor.js
	Website:
		http://bgrins.github.io/TinyColor/
	Source code:
		https://github.com/bgrins/TinyColor
	Dependencies:
		none
	Description:
		A library for converting between different (CSS) color formats.
	Why/how it is used:
		For conversion of phase colors between the format expected by angularjs-color-picker
		and that used in the backend. 
	
traverson-angular.js
	Website:
		https://github.com/basti1302/traverson (Source code and documentation of standalone version)
	Source code:
		https://github.com/basti1302/traverson-angular
	Dependencies:
		angular.js
	Description:
		Integration of Traverson and Angular. Traverson is a library for consuming REST services
		that follow the HATEOAS (Hypermedia As The Engine Of Application State) design rule, i.e.
		services that return resources with hypermedia links among each other.
	Why/how it is used:
		Because Spring Data REST indeed uses HATEOAS. Using traverson, we to only need to remember
		the URL of the entry point of the REST service, and can use an easy API for following links
		until we reach the desired resources.
	
traverson-hal.js
	Website:
		none
	Source code:
		https://github.com/basti1302/traverson-hal
	Dependencies:
		traverson-angular.js (or standalone verson)
	Description:
		Traverson plug-in for working with resources in the JSON HAL format.
	Why/how it is used:
		We are dealing with JSON HAL as the default format used by JSON HAL. Without this plug-in,
		we would need to specify a JSON Path to each link such as "$._links.linkName", while
		traverson-hal knows how to find links in a JSON HAL document, so they can be addressed
		as "linkName".
	
ui-bootstrap.js
	Website:
		https://angular-ui.github.io/bootstrap/
	Source code:
		https://github.com/angular-ui/bootstrap
	Dependencies:
		bootstrap.css
	Description:
		Angular directives for bootstrap components. 
	Why/how it is used:
		For implementing nice, non-blocking modal dialogs containing HTML forms.
		Also for implementing dropdown menues, tabs, and collapsible components.

url-template.js
	Website:
		none
	Source code:
		https://github.com/bramstein/url-template
	Dependencies:
		none
	Description:
		Implements URL templates following the RFC 6570 URI Template specification.
	Why/how it is used:
		To resolve URL templates outside the context of traverson.
		In particular, it is used to remove any templates from self-links to obtain
		unique IDs for tasks and dependencies in the diagram.
