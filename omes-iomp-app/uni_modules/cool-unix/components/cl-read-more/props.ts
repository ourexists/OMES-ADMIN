import type { PassThroughProps } from "../../types";

export type ClReadMorePassThrough = {
	className?: string;
	content?: PassThroughProps;
	mask?: PassThroughProps;
	toggle?: PassThroughProps;
};

export type ClReadMoreProps = {
	className?: string;
	pt?: ClReadMorePassThrough;
	modelValue?: boolean;
	height?: any;
	expandText?: string;
	collapseText?: string;
	expandIcon?: string;
	collapseIcon?: string;
	disabled?: boolean;
};
