from flask import Flask, render_template, url_for, request, jsonify, redirect
from dash.dependencies import Input, Output
from collections import OrderedDict

import os
import sys
import socket
import json
import pprint
import pandas               as pd
import pygmo                as pg
import plotly.graph_objs    as go
import numpy                as np

import dash
import dash_table
import dash_core_components as dcc
import dash_html_components as html
import dash_bootstrap_components as dbc

from .PlotClasses import ArchitectureInterface, MissionInterface, TradespaceInterface
from app import app


cost_objectives = ["groundCost",
				  "hardwareCost",
				  "iatCost",
				  "launchCost",
				  "lifecycleCost",
				  "nonRecurringCost",
				  "operationsCost",
				  "programCost",
				  "recurringCost"]

orbital_objectives = ["MinTime",
				  "MaxTime",
				  "MinTimeToCoverage",
				  "MaxTimeToCoverage",
				  "MeanTimeToCoverage",
				  "MinAccessTime",
				  "MaxAccessTime",
				  "MeanAccessTime",
				  "MinRevisitTime",
				  "MaxRevisitTime",
				  "MeanRevisitTime",
				  "MinResponseTime",
				  "MeanResponseTime",
				  "Coverage",
				  "MinNumOfPOIpasses",
				  "MaxNumOfPOIpasses",
				  "MeanNumOfPOIpasses",
				  "MaxDataLatency",
				  "MeanDataLatency",
				  "NumGSpassesPD",
				  "TotalDownlinkTimePD",
				  "MinDownlinkTimePerPass",
				  "MaxDownlinkTimePerPass",
				  "MeanDownlinkTimePerPass"]

instrument_objectives = ["Mean of Mean of Incidence angle [deg]",
				  "SD of Mean of Incidence angle [deg]",
				  "Mean of SD of Incidence angle [deg]",
				  "Mean of Mean of Look angle [deg]",
				  "SD of Mean of Look angle [deg]",
				  "Mean of SD of Look angle [deg]",
				  "Mean of Mean of Observation Range [km]",
				  "SD of Mean of Observation Range [km]",
				  "Mean of SD of Observation Range [km]",
				  "Mean of Mean of Noise-Equivalent delta T [K]",
				  "SD of Mean of Noise-Equivalent delta T [K]",
				  "Mean of SD of Noise-Equivalent delta T [K]",
				  "Mean of Mean of DR",
				  "SD of Mean of DR",
				  "Mean of SD of DR",
				  "Mean of Mean of SNR",
				  "SD of Mean of SNR",
				  "Mean of SD of SNR",
				  "Mean of Mean of Ground Pixel Along-Track Resolution [m]",
				  "SD of Mean of Ground Pixel Along-Track Resolution [m]",
				  "Mean of SD of Ground Pixel Along-Track Resolution [m]",
				  "Mean of Mean of Ground Pixel Cross-Track Resolution [m]",
				  "SD of Mean of Ground Pixel Cross-Track Resolution [m]",
				  "Mean of SD of Ground Pixel Cross-Track Resolution [m]",
				  "Mean of Mean of Swath-Width [m]",
				  "SD of Mean of Swath-Width [m]",
				  "Mean of SD of Swath-Width [m]",
				  "Mean of Mean of Sigma NEZ Nought [dB]",
				  "SD of Mean of Sigma NEZ Nought [dB]",
				  "Mean of SD of Sigma NEZ Nought [dB]"]

value_objectives = ["Total Architecture Value [Mbits]",
                    "Total Data Collected [Mbits]",
                    "EDA Scaling Factor"]



# GET ALL ARCHITECTURE INTERFACES
def dir_sort(x):
	return float(x[5:])


def update_interfaces():
	arch_interfaces = []
	rootdir  = '/mission'
	for subdir, dirs, files in os.walk(rootdir):
		dirs = [x for x in dirs if "arch" in x]
		for d in sorted(dirs, key = dir_sort):
			archdir = rootdir + '/' + str(d)
			temp = ArchitectureInterface(archdir)
			if(temp.is_valid()):
				arch_interfaces.append(temp)
	return arch_interfaces



all_architectures       = update_interfaces()
layout = html.Div([
	dbc.Container([
		dbc.Row([

			# LEFT COLUMN
			dbc.Col([
				html.Div([

					# RADIO BUTTONS
					dbc.FormGroup([
					        dbc.Label("Architectures", className='analysis-sats-label'),
					        dbc.Checklist(
				                options=[
				                	{'label': i.get_name(), 'value': i.get_name()} for i in all_architectures
				                ],
				                value=['arch-0','arch-1'],
				                id='architecture-checkbox',
				                className='analysis-sat-radio-class',
							    labelClassName='analysis-sat-radio-label',
				            ),
				            dcc.Interval(
					            id='interval-component-arch',
					            interval=10*1000, # in milliseconds
					            n_intervals=0
					        ),

					    ], className='text-center architecture-radio-form'
					),

					],
					className='analysis-arch-select')
			],
			width=2,className='analysis-column-left'),


			# RIGHT COLUMN
			dbc.Col([
				# ACCESS INFORMATION ROW
					dbc.Row([


						dbc.Col([

							html.Div([
								dcc.Loading(children=[
									dcc.Graph(id='value-graph')
								],
								type="default")
							],
							className='analysis-heatmap-view',id='architecture-view-div')

						],width=9,className='analysis-heatmap-column-left'),




						dbc.Col([
							html.Div([

								dbc.FormGroup(
								    [
								        dbc.Label("Orbital Metrics", className='analysis-sats-label label-margin-top'),
							            dcc.Dropdown(
										    options=[
										            	{'label': i, 'value': i} for i in orbital_objectives
										            ],
										    value=['MaxRevisitTime','MeanAccessTime'],
										    id='orbital-map-metric',
										    multi=True,
										    clearable=True,
					            			placeholder="choose orbital metric...",
					            			className='analysis-sats-label-dropdown text-left',
										),

								    ],
								    row=False,
								    className='specify-axis-format text-left'
								),

								dbc.FormGroup(
								    [
								        dbc.Label("Value Metrics", className='analysis-sats-label label-margin'),
							            dcc.Dropdown(
										    options=[
										            	{'label': i, 'value': i} for i in value_objectives
										            ],
										    value=['Total Architecture Value [Mbits]'],
										    id='value-map-metric',
										    multi=True,
										    clearable=True,
					            			placeholder="choose value metric...",
					            			className='analysis-sats-label-dropdown text-left',
										),

								    ],
								    row=False,
								    className='specify-axis-format text-left'
								),



							],
							className='analysis-heatmap-interface text-center',id='architecture-view-div') ###
						],width=3,className='analysis-heatmap-column-right'),



					],no_gutters=True,className='compare-heatmap-row'),






					# BOX AND WHISKER PLOT ROW
					dbc.Row([
						dbc.Col([
							html.Div([
								dcc.Loading(children=[
									dcc.Graph(id='snr-graph')
								],
								type="default")

							],
							className='analysis-heatmap-view',id='architecture-view-div') ###

						],width=9,className='analysis-heatmap-column-left'),




						dbc.Col([
							html.Div([



							],
							className='analysis-heatmap-interface text-center',id='architecture-view-div') ###
						],width=3,className='analysis-heatmap-column-right'),



					],no_gutters=True,className='compare-heatmap-row'),





			],
			width=10,className='analysis-column-right')


			],
			no_gutters=False,className='analysis-row',)
		],
		fluid=True,className='outter-container',)
	],
	className='',)






# ------------------- CALLBACKS ------------------- #
@app.callback(
Output('architecture-checkbox','options'),
[Input('interval-component-arch','n_intervals'),]
)
def update_architecture_list(n_intervals):
	current_archs = update_interfaces()
	options = []
	for arch in current_archs:
		options.append({'label': arch.get_name(), 'value': arch.get_name()})
	return options

@app.callback(
Output('value-graph','figure'),
[
	Input('architecture-checkbox','value'),
	Input('value-map-metric','value'),
	Input('orbital-map-metric','value')
])
def render_architecture_analysis(arch, value, orbital):
	if(arch is None):
		return [],[]
	ts_interface = TradespaceInterface('/mission')
	return render_value_radar(ts_interface, arch, value, orbital)

### ---------- RADAR PLOT ---------- ###
def render_value_radar(ts_interface, archs, value, orbital):
	polars = get_arch_polars(ts_interface, archs, value, orbital)

	return {
		'data': polars,
		'layout': go.Layout(
		    polar=dict(
		    	radialaxis=dict(
		    		visible=True,
		    		autorange=True,
		    		showgrid=False,
		    		title=dict(
		    			text='',
		    		),
		    	),

		    ),
		    showlegend=True,
		    title='Radar Plot - Normalized',
		)
	}
def get_arch_polars(ts_interface, archs, value, orbital):
	objectives = value + orbital
	data_lists = []
	arches     = []
	for arch in archs:
		arch_obj = ts_interface.get_architecture(arch)
		if arch_obj is not None:
			arches.append(arch_obj.get_name())
			temp = []
			for obj in objectives:
				temp.append(arch_obj.get_objective_data(obj))
			data_lists.append(temp)

	for x in range(0,len(objectives)):
		temp = []
		for data_list in data_lists:
			temp.append(data_list[x])
		normal = get_normal(temp)
		for y in range(0,len(data_lists)):
			data_lists[y][x] = normal[y]

	polars = []
	for x in range(0,len(data_lists)):
		polars.append(go.Scatterpolar(
		        r     = data_lists[x],
		        theta = objectives,
		        fill  = 'none',
		        name  = arches[x],
		    ))
	return polars
def get_normal(values_str):
	values = []
	for val in values_str:
		values.append(float(val))

	if(sum(values) == 0):
		return values
	return [float(i)/sum(values) for i in values]
### ---------- RADAR PLOT ---------- ###












### ---------- BOX PLOT ---------- ###
def render_snr_box(ts_interface, archs):
	boxes = []
	for arch in archs:
		arch_obj = ts_interface.get_architecture(arch)
		if arch_obj is not None:
			boxes.append(get_arch_box(arch_obj))

	return {
		'data': boxes,
		'layout': go.Layout(
		    title="SNR Box and Whisker Plot"
		)
	}

def get_arch_box(arch_obj):
	values = arch_obj.get_level_1_data('Mean of SNR')
	arch_name  = arch_obj.get_name()
	return go.Box(
		        y         = values,
		        boxpoints = 'outliers',
		        name      = arch_name,
		    )
### ---------- BOX PLOT ---------- ###
