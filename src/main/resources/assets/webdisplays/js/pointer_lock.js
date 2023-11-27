{
    const elemRef = { element: undefined, unadjusted: false };

    Document.prototype.__defineGetter__("pointerLockElement", () => {
        return elemRef['element'];
    });
    Document.prototype.__defineGetter__("webdisplays__unadjustPointerMotion", () => elemRef['unadjusted']);
    Document.prototype.__defineSetter__("pointerLockElement", (v) => {});

    Element.prototype.requestPointerLock = function(unadjustedMovement = false) {
        elemRef['element'] = this;
        elemRef['unadjusted'] = unadjustedMovement;
        document.pointerLockElement = elemRef['element'];
        document.dispatchEvent(new Event("pointerlockchange"));


        let bodyRect = document.body.getBoundingClientRect();
        let elemRect = this.getBoundingClientRect();

        window.cefQuery({
          request: 'WebDisplays_ActiveElement{exists: true,'+
            'x: ' + (elemRect.left) + ',' +
            'y: ' + (elemRect.top) + ',' +
            'w: ' + ((elemRect.right - elemRect.left)) + ',' +
            'h: ' + ((elemRect.bottom - elemRect.top)) +
          '}',
          onSuccess: function(response) {},
          onFailure: function(error_code, error_message) {}
        });
    }
    Document.prototype.exitPointerLock = () => {
        elemRef['element'] = undefined;
        elemRef['unadjusted'] = false;
        document.pointerLockElement = elemRef['element'];

        window.cefQuery({
          request: 'WebDisplays_ActiveElement{exists: false}',
          onSuccess: function(response) {},
          onFailure: function(error_code, error_message) {}
        });
    }
}
