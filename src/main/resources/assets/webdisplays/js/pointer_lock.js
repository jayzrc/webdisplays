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
    }
    Document.prototype.exitPointerLock = () => {
        elemRef['element'] = undefined;
        elemRef['unadjusted'] = false;
        document.pointerLockElement = elemRef['element'];
    }
}
