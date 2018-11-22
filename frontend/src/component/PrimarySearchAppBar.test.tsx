import {shallow} from "enzyme";
import React from 'react';
import {BrowserRouter} from "react-router-dom";
import 'reflect-metadata';
import PrimarySearchAppBar from "./PrimarySearchAppBar";

describe("component", () => {
    describe("rendering", () => {
        it('renders without crashing', () => {
            shallow(<BrowserRouter><PrimarySearchAppBar unreadCount={0}/></BrowserRouter>)
        });
    });
});