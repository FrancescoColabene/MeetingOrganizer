/**
 * Login and registration management
 */

(function() { // avoid variables ending up in the global scope

  document.getElementById("loginButton").addEventListener('click', (e) => {
    var form = e.target.closest("form");
    if (form.checkValidity()) {
      makeCall("POST", 'CheckLogin', form,
        function(x) {
          if (x.readyState == XMLHttpRequest.DONE) {
            var message = x.responseText;
            switch (x.status) {
              case 200:
            	sessionStorage.setItem('username', message);
                window.location.href = "HomePage.html";
                break;
              case 400: // bad request
              case 401: // unauthorized
              case 500: // server error
              default:
                document.getElementById("message").textContent = message;
                break;
            }
          }
        }
      );
    } else {
    	 form.reportValidity();
    }
  });

})();

(function() {
	
  document.getElementById("password").addEventListener('keypress', (e) => {
    if(e.key === "Enter") { 
	  e.preventDefault();
	  document.getElementById("loginButton").click();
	}
	});	
})();


(function() { // avoid variables ending up in the global scope

  document.getElementById("registrationButton").addEventListener('click', (e) => {
    var form = e.target.closest("form");
    if (form.checkValidity()) {
	  var email = document.getElementById("emailReg");
	  const emailRegExp = /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*$/;
	  const emailTest = email.value.length === 0 || emailRegExp.test(email.value);
	  if(!emailTest){
		document.getElementById("message").textContent = "Insert a valid email";
		return;
	  };
	  var pwd1 = document.getElementById("pwd1Reg");
	  var pwd2 = document.getElementById("pwd2Reg");
	  if(  pwd1.value != pwd2.value ) {
		document.getElementById("message").textContent = "Passwords do not match";
		return;
	  }
      makeCall("POST", 'CreateAccount', form,
        function(x) {
          if (x.readyState == XMLHttpRequest.DONE) {
            var message = x.responseText;
            switch (x.status) {
              case 200:
				  document.getElementById("message").textContent = "The creation of the account was successful";
                  var input = form.getElementsByTagName("input");
                  for( let i = 0 ; i < input.length - 1 ; i++ ){
				    input[i].value = "";
                  }
                break;
              case 400: // bad request
              case 401: // unauthorized
              case 500: // server error
                document.getElementById("message").textContent = message;
                break;
            }
          }
        }
      );
    } else {
    	 form.reportValidity();
    }
  });

})();

(function() {
	
  document.getElementById("pwd2Reg").addEventListener('keypress', (e) => {
    if(e.key === "Enter") { 
	  e.preventDefault();
	  document.getElementById("registrationButton").click();
	}
	});	
})();
