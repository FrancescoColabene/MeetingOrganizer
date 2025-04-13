(function(){
	let hostedMeetings, invitedMeetings, wizard,
		pageOrchestrator = new PageOrchestrator();
	
	window.addEventListener("load", () => {
	    if (sessionStorage.getItem("username") == null) {
	      window.location.href = "index.html";
	    } else {
	      pageOrchestrator.start(); // initialize the components
	      pageOrchestrator.refresh();
	    } // display initial content
	  }, false);
	  
	function welcomeMessage(_username, messageContainer){
		this.username = _username;
	    this.show = function() {
	      messageContainer.textContent = this.username;
	    }
	}
	
	function meetings(_alert, _listcontainer, _listcontainerbody, _flag){
		this.alert = _alert;
	    this.listcontainer = _listcontainer;
	    this.listcontainerbody = _listcontainerbody;
	    this.flag = _flag;

	    this.reset = function() {
	      this.listcontainer.style.visibility = "hidden";
	    }

	    this.show = function() {
	      var self = this;
	      var method = (_flag) ? "GetInvitedMeetings" : "GetHostedMeetings";
	      makeCall("GET", method, null,
	        function(req) {
	          if (req.readyState == XMLHttpRequest.DONE) {
	            var message = req.responseText;
	            if (req.status == 200) {
	              var meetingToShow = JSON.parse(req.responseText);
	              if (meetingToShow.length == 0) {
	                self.alert.textContent = "No meetings yet!";
	                return;
	              }
	              self.update(meetingToShow); // self visible by closure
	              // qui potrei già ordinare in base alla data! 
	            
	          } else if (req.status == 403) {
                  window.location.href = req.getResponseHeader("Location");
                  window.sessionStorage.removeItem('username');
                  }
                  else {
	            self.alert.textContent = message;
	          }}
	        }
	      );
	    };


	    this.update = function(arrayMeetings) {
	      var row, cell;
	      this.listcontainerbody.innerHTML = ""; // empty the table body
	      // build updated list
	      var self = this;
	      arrayMeetings.forEach(function(meeting) { // self visible here, not this
	        row = document.createElement("tr");
	        if(self.flag){
			  cell = document.createElement("td");
	          cell.textContent = meeting.fullNameHost;
	          row.appendChild(cell);
			}
	        cell = document.createElement("td");
	        cell.textContent = meeting.title;
	        row.appendChild(cell);
	        cell = document.createElement("td");
	        cell.textContent = meeting.dataStart;
	        row.appendChild(cell);
	        cell = document.createElement("td");
	        let hs = meeting.hourStart;
	        let h = Number(hs.match(/^(\d+)/)[1]);
	        let m = Number(hs.match(/:(\d+)/)[1]);
	        let hf = (hs.match(/\s(.*)$/)[1] === "PM") ? 
	        	12 + h : h;
	        if(hf/10 < 1) hf = "0" + hf.toString();
	        if(m/10 < 1) m = "0" + m.toString();
	        let d = hf + ":" + m;
	        cell.textContent = d;
	        row.appendChild(cell);
	        cell = document.createElement("td");
	        hs = meeting.duration;
	        h = Number(hs.match(/^(\d+)/)[1]);
	        m = Number(hs.match(/:(\d+)/)[1]);
	        hf = (hs.match(/\s(.*)$/)[1] === "PM") ? 
	        	12 + h : h;
	        if(hf/10 < 1) hf = "0" + hf.toString();
	        if(m/10 < 1) m = "0" + m.toString();
			d = hf + ":" + m;
	        cell.textContent = d;
	        row.appendChild(cell);
	        self.listcontainerbody.appendChild(row);
	      });
	      this.listcontainer.style.visibility = "visible";
	    }
	}
	
	// serve per la creazione di una missione nel progetto del prof, è da sostituire con il controllo dei campi della riunione + finestra modale
	
	function MeetingCreation(options) {
		this.alert1 = options['alert1'], 
		this.alert2=options['alert2'],
	    this.title,
		this.date,
		this.hour,
		this.duration,
		this.max,
		this.meeting=options['meetingId'],
		this.contentContainer=options['invitesContainer'],
		this.modal=options['modal'],
		this.inviteTable=options['inviteTable'],
		this.counter=0;
		this.flag=false;
	    
	    var now = new Date(),
	      formattedDate = now.toISOString().substring(0, 10);
	    this.meeting.querySelector('input[type="date"]').setAttribute("min", formattedDate);

	    this.registerEvents = function(orchestrator) {
		var self = this;
	      document.getElementById("openModal").addEventListener('click', (e) => {
			var form = e.target.closest("form");
			this.title = document.getElementById("title").value;
	    	this.date = document.getElementById("date").value;
	    	this.hour = document.getElementById("hour").value;
	    	this.duration = document.getElementById("duration").value;
	    	this.max = document.getElementById("max").value;
	    	this.counter = 0;
			if(form.checkValidity()){
			  if(self.title.length == 0){
				self.alert1.textContent = "You have to insert a valid title";
				return;
			  }
			  if(self.title.length > 45) {
				self.alert1.textContent = "The title is too long, it cannot exceed 45 characters";
				return;
			  }
			  if ( self.duration == "00:00" ){
				self.alert1.textContent = "The duration is zero, please modify it";
				return;
			  }
			  if( isNaN(self.max) || self.max < 2 ) {
				self.alert1.textContent = "You can't create a meeting without people";
				return;
			  }
			  if (self.date == ""){
				self.alert1.textContent = "You have to select a valid date";
				return;
			  }
			  let d = new Date(self.date);
			  let h = Number(self.hour.match(/^(\d+)/)[1]);
	          let m = Number(self.hour.match(/:(\d+)/)[1]);
			  d.setHours(h);
			  d.setMinutes(m);
			  let today = new Date();
			  if ( today.getTime() > d.getTime() ) {
				self.alert1.textContent = "The selected date has already passed";
				return;
			  }
			  document.getElementById("hiddenTitle").value = self.title;
			  makeCall("POST", 'OpenAnagrafica', form,
        		function(x) {
          		  if (x.readyState == XMLHttpRequest.DONE) {
					var message = x.responseText;
					// se è corretto, il messaggio è la mappa checks
            		if ( x.status === 200 ) {
					  self.flag = true;
            		  // fare robe per finestra modale
            		  self.modal.style.display = "block";
            		  // setto info
            		  document.getElementById("titleA").textContent = self.title;
            		  document.getElementById("dataA").textContent = self.date;
            		  document.getElementById("hourA").textContent = self.hour;
           			  document.getElementById("durationA").textContent = self.duration;
           			  document.getElementById("maxA").textContent = self.max-1;
           			  // fa tabella con le checkbox
           			  let checks = JSON.parse(x.responseText);
					  self.update(checks);
					  
            		}
            		else if ( x.status === 403 ){
					  window.location.href = req.getResponseHeader("Location");
                  	  window.sessionStorage.removeItem('username');
					}
					else {
	            	  self.alert1.textContent = message;
	            	  //self.alert1.textContent = "Something went wrong, insert correct values";
	          		}
	          	}
          	  });
			} else {
			  form.reportValidity();
			}
		  });
		  
		  document.getElementById("cross").addEventListener('click', (e) => {
			this.clear();
			self.inviteTable.style.display = "block";
			self.alert2.textContent = "";
			if(self.flag){
			  let form = document.createElement("form");
			  let x = document.createElement("input");
			  x.value = self.title;
			  x.name = "title";
			  form.appendChild(x);
			  makeCall("POST", 'DeleteMeeting', form, function(ignore){});
			  self.flag=false;
			}
		  });
		  
		  window.addEventListener('click', (e) => {
			if(e.target == this.modal){
				this.clear();
				self.inviteTable.style.display = "block";
				self.alert2.textContent = "";
				if(self.flag){
				  let form = document.createElement("form");
			  	  let x = document.createElement("input");
			  	  x.value = self.title;
			  	  x.name = "title";
			  	  form.appendChild(x);
				  makeCall("POST", 'DeleteMeeting', form, function(ignore){});
				  self.flag=false;
				}
			}
		  });
		  
		  document.getElementById("createMeeting").addEventListener('click', (e) => {
			// controllare che non siano oltre il limite e poi inviare la richiesta. se ho esito positivo, faccio this.clear() e orchestator.refresh()
			// se ho fatto 3 tentativi con troppe persone faccio this.clear()
			let temp = document.getElementsByName("invited");
			let n = 0;
			temp.forEach(cb => {
			  if(cb.checked) n++;
			});
			if(n === 0){
			  self.alert2.textContent = "You have to select at least one partecipant";
			  return;
			}
			if (n < self.max){
			  makeCall("POST", 'CreateMeeting', e.target.closest("form"),
        		function(x) {
				  if (x.readyState == XMLHttpRequest.DONE) {
           			var message = x.responseText;
				    self.counter++;
				    if(x.status === 500){
					  orchestrator.refresh();
					  self.alert1.textContent = "There was problems with creating the meeting";
					  self.inviteTable.style.display = "none";
					}
				    if(message == 0){
					  // tolgo la finestra modale
					  self.flag = false;
					  orchestrator.refresh();
					} else if (message > 0){
					  self.alert2.textContent = "You have selected too many partecipants, you have to remove at least " + message 
					  								+ " of them. Remaining attempts: " + (3 - self.counter);
					}
					if(message === -1){
					  self.flag = false;
					  self.alert2.textContent = "Three tries of defining a meeting with too many partecipants, the meeting will not be created";
					  self.inviteTable.style.display = "none";
					  // dovrei togliere dalla visualizzazione la tabella e le altre scritte
					}
					if( message === -2 ){
					  self.flag = false;
					  self.alert2.textContent = "Problems with the server, the meeting will not be created";
					  self.reset();
					}
				  }
				});
	    	} else {
			  self.counter++;
			  if( self.counter === 3 ){
				self.flag=false;
				self.alert2.textContent = "Three tries of defining a meeting with too many partecipants, the meeting will not be created";
				let form = document.createElement("form");
			    let x = document.createElement("input");
			    x.value = self.title;
			    x.name = "title";
			    form.appendChild(x);
				makeCall("POST", 'DeleteMeeting', form, function(ignore){});
				self.inviteTable.style.display = "none";
			  } else {
				self.alert2.textContent = "You have selected too many partecipants, you have to remove at least " + (n+1-self.max)
					  								+ " of them. Remaining attempts: " + (3 - self.counter);
			  }
	        }
	      });
	    };
	    
	    this.update = function(checks){
		  var row, cell, check, n=0;
		  this.contentContainer.innerHTML = ""; // empty the table body
		  var self = this;
		  const map = new Map(Object.entries(checks));
		  
		  // funziona, ma non riesce a prendersi i parametri della persona. forse trasformandolo in un vettore riesco
		  for(const [key,value] of map){
			res = key.split(',');
			const id = res[0].split('=')[1];
			const name = res[2].split('=')[1];
			const surname = res[3].split('=')[1].slice(0,-1);
			
			row = document.createElement("tr");
	        cell = document.createElement("td");
	        cell.textContent = name;
	        row.appendChild(cell);
	        cell = document.createElement("td");
	        cell.textContent = surname;
	        row.appendChild(cell);
	        cell = document.createElement("td");
	        check = document.createElement("input");
	        check.type = "checkbox";
	        check.name = "invited";
	        check.value = id;
	        check.id = n;
	        cell.appendChild(check);
	        row.appendChild(cell);
	        self.contentContainer.appendChild(row);
			n++;
		  }
		  this.contentContainer.style.visibility = "visible";
		}
		    
	    this.clear = function() {
		  this.modal.style.display = "none";
		}
		
	  }
	
	
	function PageOrchestrator(){
	    
	    alertHostedContainer = document.getElementById("id_alertHosted");
	    alertInvitedContainer = document.getElementById("id_alertInvited");
	    alertInvitesContainer = document.getElementById("id_alertInvites");
	    
	    // crea lo scheletro dell'interfaccia
	    this.start = function() {
	      welcomeMessage = new welcomeMessage(sessionStorage.getItem('username'),
	        document.getElementById("id_welcome"));
	      welcomeMessage.show();

	      hostedMeetings = new meetings(
	        alertHostedContainer,
	        document.getElementById("id_hosted"),
	        document.getElementById("id_hostedbody"),
	        false);
	      invitedMeetings = new meetings(
	        alertInvitedContainer,
	        document.getElementById("id_invited"),
	        document.getElementById("id_invitedbody"),
	        true);

		  // robe
		  wizard = new MeetingCreation({
			alert1 : document.getElementById("id_alertCreation"),
			alert2 : document.getElementById("id_alertInvites"),
			meetingId : document.getElementById("id_meetingForm"),
			invitesContainer : document.getElementById("id_peopleInvitesbody"),
			modal : document.getElementById("anagraficaModal"),
			inviteTable : document.getElementById("inviteTable")
		  });

		  wizard.registerEvents(this);

	      document.querySelector("a[href='Logout']").addEventListener('click', () => {
	        window.sessionStorage.removeItem('username');
	      })
	    };
	    
	    // carichiamo i contenuti / visualizziamo i componenti
	    this.refresh = function() {
	      alertHostedContainer.textContent = "";
	      alertInvitedContainer.textContent = ""; 
	      alertInvitesContainer.textContent = ""; 
	      hostedMeetings.reset();
	      invitedMeetings.reset();
	      hostedMeetings.show();
	      invitedMeetings.show();
	      wizard.clear();
	    };
	    
	  }
})();