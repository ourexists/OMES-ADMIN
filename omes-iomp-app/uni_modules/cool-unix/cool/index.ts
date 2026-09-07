import { scroller } from "./scroller";

export const cool = (app: VueApp) => {
	app.mixin({
		onPageScroll(e) {
			scroller.emit(e.scrollTop);
		}
	});
};

export * from "./animation";
export * from "./ctx";
export * from "./hooks";
export * from "./router";
export * from "./scroller";
export * from "./utils";
export * from "./types";
