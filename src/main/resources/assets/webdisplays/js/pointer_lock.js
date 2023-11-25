{
    const elemRef = { element: undefined };

    Document.prototype.__defineGetter__("pointerLockElement", () => elemRef['element']);
    Document.prototype.__defineSetter__("pointerLockElement", (v) => {});

    Element.prototype.requestPointerLock = function(unadjustedMovement = false) {
        elemRef['element'] = this;
        document.pointerLockElement = elemRef['element'];
        document.dispatchEvent(new Event("pointerlockchange"));
    }
    Document.prototype.exitPointerLock = () => {
        elemRef['element'] = undefined;
        document.pointerLockElement = elemRef['element'];
    }
}
